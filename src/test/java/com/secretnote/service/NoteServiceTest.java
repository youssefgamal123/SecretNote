package com.secretnote.service;

import com.secretnote.dto.requests.NoteCreationRequest;
import com.secretnote.dto.requests.NoteUpdateRequest;
import com.secretnote.dto.response.NoteResponse;
import com.secretnote.entity.Note;
import com.secretnote.entity.User;
import com.secretnote.exception.ApiException;
import com.secretnote.repository.NoteRepository;
import com.secretnote.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock private NoteRepository noteRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private NoteService noteService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("owner@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser.getEmail(), "password"));
        lenient().when(userRepository.findByEmail(currentUser.getEmail())).thenReturn(Optional.of(currentUser));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createNoteSavesForCurrentUserAndReturnsMappedResponse() {
        NoteCreationRequest request = new NoteCreationRequest();
        request.setTitle("Secret");
        request.setContent("Text");
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(12L);
            return note;
        });

        NoteResponse response = noteService.createNote(request);

        assertEquals(12L, response.getId());
        assertEquals("Secret", response.getTitle());
        assertEquals("Text", response.getContent());
        assertEquals("Note created successfully", response.getMessage());
        verify(noteRepository).save(argThat(note -> note.getUser().equals(currentUser)
                && note.getCreatedAt() != null && note.getUpdatedAt() != null));
    }

    @Test
    void getUserNotesMapsRepositoryResults() {
        Note note = Note.builder().id(4L).title("Title").content("Body").user(currentUser).build();
        when(noteRepository.findByUser(currentUser)).thenReturn(List.of(note));

        List<NoteResponse> responses = noteService.getUserNotes();

        assertEquals(1, responses.size());
        assertEquals(4L, responses.get(0).getId());
        assertEquals("Fetched successfully", responses.get(0).getMessage());
        verify(noteRepository).findByUser(currentUser);
    }

    @Test
    void getNoteByIdReturnsNormalNoteWithoutDeletingIt() {
        Note note = Note.builder().id(4L).title("Title").content("Body").user(currentUser).build();
        when(noteRepository.findById(4L)).thenReturn(Optional.of(note));

        NoteResponse response = noteService.getNoteById(4L);

        assertEquals("This is a normal note.", response.getMessage());
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void getNoteByIdDeletesSelfDestructiveNoteAndReturnsItsContent() {
        Note note = Note.builder().id(4L).title("One time").content("Body").user(currentUser)
                .selfDestructive(true).build();
        when(noteRepository.findById(4L)).thenReturn(Optional.of(note));

        NoteResponse response = noteService.getNoteById(4L);

        assertEquals("Body", response.getContent());
        assertEquals("This was a self-destructive note. It has now been deleted.", response.getMessage());
        verify(noteRepository).delete(note);
    }

    @Test
    void getNoteByIdDeletesExpiredSelfDestructiveNoteAndThrowsGone() {
        Note note = Note.builder().id(4L).user(currentUser).selfDestructive(true)
                .expiresAt(LocalDateTime.now().minusSeconds(1)).build();
        when(noteRepository.findById(4L)).thenReturn(Optional.of(note));

        ApiException exception = assertThrows(ApiException.class, () -> noteService.getNoteById(4L));

        assertEquals(HttpStatus.GONE, exception.getStatus());
        verify(noteRepository).delete(note);
    }

    @Test
    void getNoteByIdRejectsNoteOwnedByAnotherUser() {
        User otherUser = User.builder().id(2L).email("other@example.com").build();
        Note note = Note.builder().id(4L).user(otherUser).build();
        when(noteRepository.findById(4L)).thenReturn(Optional.of(note));

        ApiException exception = assertThrows(ApiException.class, () -> noteService.getNoteById(4L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void getNoteByIdReportsMissingNote() {
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> noteService.getNoteById(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void updateNoteChangesFieldsAndSaves() {
        Note note = Note.builder().id(4L).title("Old").content("Old body").user(currentUser).build();
        when(noteRepository.findById(4L)).thenReturn(Optional.of(note));
        when(noteRepository.save(note)).thenReturn(note);
        NoteUpdateRequest request = new NoteUpdateRequest();
        request.setTitle("New");
        request.setContent("New body");

        NoteResponse response = noteService.updateNote(4L, request);

        assertEquals("New", response.getTitle());
        assertEquals("New body", response.getContent());
        assertEquals("Note updated successfully", response.getMessage());
        assertNotNull(note.getUpdatedAt());
        verify(noteRepository).save(note);
    }

    @Test
    void updateNoteRejectsOtherOwnersAndDoesNotSave() {
        User otherUser = User.builder().id(2L).build();
        Note note = Note.builder().id(4L).user(otherUser).build();
        when(noteRepository.findById(4L)).thenReturn(Optional.of(note));

        ApiException exception = assertThrows(ApiException.class,
                () -> noteService.updateNote(4L, new NoteUpdateRequest()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void deleteNoteDeletesOwnedNote() {
        Note note = Note.builder().id(4L).user(currentUser).build();
        when(noteRepository.findById(4L)).thenReturn(Optional.of(note));

        noteService.deleteNote(4L);

        verify(noteRepository).delete(note);
    }

    @Test
    void deleteNoteRejectsOtherOwners() {
        User otherUser = User.builder().id(2L).build();
        Note note = Note.builder().id(4L).user(otherUser).build();
        when(noteRepository.findById(4L)).thenReturn(Optional.of(note));

        ApiException exception = assertThrows(ApiException.class, () -> noteService.deleteNote(4L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void serviceRejectsUnauthenticatedCall() {
        SecurityContextHolder.clearContext();

        ApiException exception = assertThrows(ApiException.class, () -> noteService.getUserNotes());

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verifyNoInteractions(userRepository, noteRepository);
    }
}
