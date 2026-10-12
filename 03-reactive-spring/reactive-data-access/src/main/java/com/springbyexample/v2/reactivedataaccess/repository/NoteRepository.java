package com.springbyexample.v2.reactivedataaccess.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.springbyexample.v2.reactivedataaccess.entity.Note;

import reactor.core.publisher.Flux;

/**
 * @author Mujuzi Moses
 */
public interface NoteRepository extends ReactiveCrudRepository<Note, Long> {

    Flux<Note> findByTitleContainingIgnoreCase(String title);
}
