package com.springbyexample.v2.reactivedataaccess.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.springbyexample.v2.reactivedataaccess.entity.Note;
import com.springbyexample.v2.reactivedataaccess.repository.NoteRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void shouldFindAllNotes() {
        when(noteRepository.findAll()).thenReturn(Flux.just(
                new Note(1L, "Reactive Spring", "R2DBC"),
                new Note(2L, "WebFlux", "Reactive HTTP")));

        StepVerifier.create(noteService.findAll()).expectNextCount(2).verifyComplete();
    }

    @Test
    void shouldFindNoteById() {
        Note note = new Note(1L, "Reactive Spring", "R2DBC");
        when(noteRepository.findById(1L)).thenReturn(Mono.just(note));

        StepVerifier.create(noteService.findById(1L)).expectNext(note).verifyComplete();
    }

    @Test
    void shouldSearchNotesByTitle() {
        Note note = new Note(1L, "Reactive Spring", "R2DBC");
        when(noteRepository.findByTitleContainingIgnoreCase("spring")).thenReturn(Flux.just(note));

        StepVerifier.create(noteService.searchByTitle("spring")).expectNext(note).verifyComplete();
    }

    @Test
    void shouldCreateNoteWithoutExistingId() {
        Note input = new Note(99L, "Reactive Spring", "R2DBC");
        Note saved = new Note(1L, "Reactive Spring", "R2DBC");

        when(noteRepository.save(any(Note.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(noteService.create(input))
                .assertNext(note -> {
                    assertThat(note.id()).isEqualTo(1L);
                    assertThat(note.title()).isEqualTo("Reactive Spring");
                    assertThat(note.content()).isEqualTo("R2DBC");
                }).verifyComplete();

        verify(noteRepository).save(new Note(null, input.title(), input.content()));
    }

    @Test
    void shouldUpdateExistingNote() {
        Note existing = new Note(1L, "Old title", "Old content");
        Note input = new Note(null, "Updated title", "Updated content");
        Note updated = new Note(1L, "Updated title", "Updated content");

        when(noteRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(noteRepository.save(updated)).thenReturn(Mono.just(updated));

        StepVerifier.create(noteService.update(1L, input)).expectNext(updated).verifyComplete();

        verify(noteRepository).save(updated);
    }

    @Test
    void shouldCompleteWithoutUpdatingWhenNoteDoesNotExist() {
        Note input = new Note(null, "Updated title", "Updated content");

        when(noteRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(noteService.update(99L, input))
                .verifyComplete();

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void shouldDeleteExistingNote() {
        when(noteRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(noteRepository.deleteById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(noteService.deleteById(1L)).expectNext(true).verifyComplete();

        verify(noteRepository).deleteById(1L);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonexistentNote() {
        when(noteRepository.existsById(99L)).thenReturn(Mono.just(false));

        StepVerifier.create(noteService.deleteById(99L)).expectNext(false).verifyComplete();

        verify(noteRepository, never()).deleteById(99L);
    }
}
