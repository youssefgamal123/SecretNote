package com.secretnote.controller;


import com.secretnote.dto.requests.NoteCreationRequest;
import com.secretnote.dto.requests.NoteUpdateRequest;
import com.secretnote.dto.response.NoteResponse;
import com.secretnote.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@RequestBody NoteCreationRequest request) {
        return ResponseEntity.ok(noteService.createNote(request));
    }


    @GetMapping
    public ResponseEntity<List<NoteResponse>> getUserNotes() {
        return ResponseEntity.ok(noteService.getUserNotes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }



    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @RequestBody NoteUpdateRequest request
    ) {
        return ResponseEntity.ok(noteService.updateNote(id, request));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok("Note deleted successfully");
    }




}
