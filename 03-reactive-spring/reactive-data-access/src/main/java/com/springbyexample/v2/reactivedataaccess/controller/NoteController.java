package com.springbyexample.v2.reactivedataaccess.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.reactivedataaccess.entity.Note;
import com.springbyexample.v2.reactivedataaccess.service.NoteService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public Flux<Note> findAll() {
        return noteService.findAll();
    }

    @GetMapping("/search")
    public Flux<Note> searchByTitle(@RequestParam String title) {
        return noteService.searchByTitle(title);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Note>> findById(@PathVariable Long id) {
        return noteService.findById(id).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Note>> create(@RequestBody Note note) {
        return noteService.create(note).map(saved -> ResponseEntity.created(URI.create("/api/notes/" + saved.id()))
                .body(saved));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Note>> update(@PathVariable Long id, @RequestBody Note note) {
        return noteService.update(id, note).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return noteService.deleteById(id).map(deleted -> deleted ? ResponseEntity.noContent().<Void>build()
                : ResponseEntity.notFound().<Void>build());
    }
}
