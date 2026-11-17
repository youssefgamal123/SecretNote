package com.secretnote.service;

import com.secretnote.dto.requests.AuthenticationRequest;
import com.secretnote.dto.requests.RegisterRequest;
import com.secretnote.dto.response.AuthenticationResponse;
import com.secretnote.entity.User;
import com.secretnote.enums.Role;
import com.secretnote.exception.ApiException;
import com.secretnote.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private UserService userService;

    @Test
    void registerCreatesUserWithEncodedPasswordAndUserRoleThenReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("person@example.com");
        request.setPassword("plain");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthenticationResponse response = userService.register(request);

        assertEquals("jwt-token", response.getToken());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("person@example.com", userCaptor.getValue().getEmail());
        assertEquals("encoded", userCaptor.getValue().getPassword());
        assertEquals(Set.of(Role.ROLE_USER), userCaptor.getValue().getRoles());
        verify(passwordEncoder).encode("plain");
    }

    @Test
    void registerRejectsExistingEmailWithoutSaving() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("person@example.com");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(User.builder().build()));

        ApiException exception = assertThrows(ApiException.class, () -> userService.register(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void authenticateReturnsTokenAfterSuccessfulAuthenticationAndUserLookup() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("person@example.com");
        request.setPassword("plain");
        User user = User.builder().email(request.getEmail()).password("encoded").roles(Set.of(Role.ROLE_USER)).build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthenticationResponse response = userService.authenticate(request);

        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail(request.getEmail());
        verify(jwtService).generateToken(any());
    }

    @Test
    void authenticateTranslatesBadCredentialsToUnauthorizedApiException() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("person@example.com");
        request.setPassword("wrong");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        ApiException exception = assertThrows(ApiException.class, () -> userService.authenticate(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void authenticateReportsMissingUserAfterAuthentication() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("person@example.com");
        request.setPassword("plain");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> userService.authenticate(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtService);
    }
}
