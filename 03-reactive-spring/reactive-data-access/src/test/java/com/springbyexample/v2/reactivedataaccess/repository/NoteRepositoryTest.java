package com.springbyexample.v2.reactivedataaccess.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;

import com.springbyexample.v2.reactivedataaccess.entity.Note;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
@DataR2dbcTest
public class NoteRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Test
    void shouldSaveAndRetrieveNote() {
        Note note = new Note(null, "Reactive Spring", "Learn R2DBC");

        StepVerifier.create(noteRepository.save(note).flatMap(saved -> noteRepository.findById(saved.id())))
                .assertNext(saved -> {
                    assertThat(saved.id()).isNotNull();
                    assertThat(saved.title()).isEqualTo("Reactive Spring");
                    assertThat(saved.content()).isEqualTo("Learn R2DBC");
                }).verifyComplete();
    }

    @Test
    void shouldFindNotesByTitleIgnoringCase1() {
        Flux<Note> notes = noteRepository.deleteAll().thenMany(noteRepository.saveAll(Flux.just(
                        new Note(null, "Reactive Spring", "R2DBC example"),
                        new Note(null, "Java Basics", "Java example"))))
                .thenMany(noteRepository.findByTitleContainingIgnoreCase("spring"));

        StepVerifier.create(notes)
                .assertNext(note -> assertThat(note.title()).isEqualTo("Reactive Spring"))
                .expectNextCount(0).verifyComplete();
    }
}
