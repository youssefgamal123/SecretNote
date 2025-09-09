package com.secretnote.service;


import com.secretnote.dto.requests.NoteCreationRequest;
import com.secretnote.dto.requests.NoteUpdateRequest;
import com.secretnote.dto.response.NoteResponse;
import com.secretnote.entity.Note;
import com.secretnote.entity.User;
import com.secretnote.repository.NoteRepository;
import com.secretnote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NoteService {


    private final NoteRepository noteRepository;
    private final UserRepository userRepository;


    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UsernameNotFoundException("User is not Authenticated , Please Login");
        }


        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(  () -> new UsernameNotFoundException("User not found") );


    }

    private NoteResponse mapToResponse(Note note,String message) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .message(message)
                .build();
    }



    public NoteResponse createNote(NoteCreationRequest request) {

        User currentUser = getCurrentUser();
        Note note = Note.builder()
                .title(request.getTitle())
                .content(request.getContent()   )
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(currentUser)
                .build();


         noteRepository.save(note);
        return mapToResponse(note, "Note created successfully");
    }



    public List<NoteResponse>getUserNotes() {
    User currentUser = getCurrentUser();

    return noteRepository.findByUser(currentUser).stream()
            .map(note -> mapToResponse(note, "Fetched successfully"))
            .collect(Collectors.toList());


    }


    public NoteResponse getNoteById(Long id) {
        User currentUser = getCurrentUser();

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getUser().equals(currentUser)) {
            throw new RuntimeException("Access denied");
        }

        if (note.isSelfDestructive()) {
            if (note.getExpiresAt() != null && LocalDateTime.now().isAfter(note.getExpiresAt())) {
                noteRepository.delete(note);
                throw new RuntimeException("This self-destructive note has already expired and was deleted.");
            }


            noteRepository.delete(note);
            return mapToResponse(note, "This was a self-destructive note. It has now been deleted.");
        }

        return mapToResponse(note, "This is a normal note.");
    }


    public NoteResponse updateNote(Long id, NoteUpdateRequest request) {
        User currentUser = getCurrentUser();

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to modify this note");
        }

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setUpdatedAt(LocalDateTime.now());

        Note updatedNote = noteRepository.save(note);

        return mapToResponse(updatedNote, "Note updated successfully");
    }


    public void deleteNote(Long id) {
        User currentUser = getCurrentUser();

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to delete this note");
        }

        noteRepository.delete(note);
    }





}
