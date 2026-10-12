package com.springbyexample.v2.reactivedataaccess.controller;

import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.springbyexample.v2.reactivedataaccess.entity.Note;
import com.springbyexample.v2.reactivedataaccess.service.NoteService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@WebFluxTest(NoteController.class)
public class NoteControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private NoteService noteService;

    @Test
    void shouldReturnAllNotes() {
        given(noteService.findAll()).willReturn(Flux.just(
                new Note(1L, "Reactive Spring", "R2DBC"),
                new Note(2L, "WebFlux", "Reactive HTTP")));

        webTestClient.get()
                .uri("/api/notes").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$[0].title").isEqualTo("Reactive Spring")
                .jsonPath("$[1].title").isEqualTo("WebFlux");
    }

    @Test
    void shouldReturnNoteById() {
        given(noteService.findById(1L)).willReturn(Mono.just(new Note(1L, "Reactive Spring", "R2DBC")));

        webTestClient.get()
                .uri("/api/notes/1").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Reactive Spring");
    }

    @Test
    void shouldReturnNotFoundWhenNoteDoesNotExist() {
        given(noteService.findById(99L)).willReturn(Mono.empty());

        webTestClient.get().uri("/api/notes/99").exchange().expectStatus().isNotFound();
    }

    @Test
    void shouldSearchNotesByTitle() {
        given(noteService.searchByTitle("spring")).willReturn(Flux.just(
                new Note(1L, "Reactive Spring", "R2DBC")));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/notes/search").queryParam("title", "spring")
                        .build()).exchange().expectStatus().isOk().expectBody()
                .jsonPath("$[0].title").isEqualTo("Reactive Spring");
    }

    @Test
    void shouldCreateNote() {
        Note input = new Note(null, "Reactive Spring", "R2DBC");
        Note saved = new Note(1L, "Reactive Spring", "R2DBC");

        given(noteService.create(input)).willReturn(Mono.just(saved));

        webTestClient.post()
                .uri("/api/notes").bodyValue(input).exchange().expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/notes/1")
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Reactive Spring");
    }

    @Test
    void shouldUpdateExistingNote() {
        Note input = new Note(null, "Updated title", "Updated content");
        Note updated = new Note(1L, "Updated title", "Updated content");

        given(noteService.update(1L, input)).willReturn(Mono.just(updated));

        webTestClient.put()
                .uri("/api/notes/1").bodyValue(input).exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.title").isEqualTo("Updated title");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentNote() {
        Note input = new Note(null, "Updated title", "Updated content");

        given(noteService.update(99L, input)).willReturn(Mono.empty());

        webTestClient.put().uri("/api/notes/99").bodyValue(input).exchange().expectStatus().isNotFound();
    }

    @Test
    void shouldDeleteExistingNote() {
        given(noteService.deleteById(1L)).willReturn(Mono.just(true));

        webTestClient.delete().uri("/api/notes/1").exchange().expectStatus().isNoContent().expectBody().isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonexistentNote() {
        given(noteService.deleteById(99L)).willReturn(Mono.just(false));

        webTestClient.delete().uri("/api/notes/99").exchange().expectStatus().isNotFound();
    }
}