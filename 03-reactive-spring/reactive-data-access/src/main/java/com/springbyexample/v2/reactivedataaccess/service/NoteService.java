package com.springbyexample.v2.reactivedataaccess.service;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.reactivedataaccess.entity.Note;
import com.springbyexample.v2.reactivedataaccess.repository.NoteRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Flux<Note> findAll() {
        return noteRepository.findAll();
    }

    public Mono<Note> findById(Long id) {
        return noteRepository.findById(id);
    }

    public Flux<Note> searchByTitle(String title) {
        return noteRepository.findByTitleContainingIgnoreCase(title);
    }

    public Mono<Note> create(Note note) {
        return noteRepository.save(new Note(null, note.title(), note.content()));
    }

    public Mono<Note> update(Long id, Note note) {
        return noteRepository.findById(id).flatMap(existing -> noteRepository.save(
                new Note(existing.id(), note.title(), note.content())));
    }

    public Mono<Boolean> deleteById(Long id) {
        return noteRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.just(false);
            }
            return noteRepository.deleteById(id).thenReturn(true);
        });
    }
}
