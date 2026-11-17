package com.secretnote.service;

import com.secretnote.entity.User;
import com.secretnote.enums.Role;
import com.secretnote.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretNoteUserDetailsServiceImplTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private SecretNoteUserDetailsServiceImpl service;

    @Test
    void loadUserByUsernameMapsUserAndRolesToSpringSecurityDetails() {
        User user = User.builder().email("person@example.com").password("encoded")
                .roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN)).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername(user.getEmail());

        assertEquals("person@example.com", details.getUsername());
        assertEquals("encoded", details.getPassword());
        assertEquals(Set.of("ROLE_USER", "ROLE_ADMIN"),
                details.getAuthorities().stream().map(Object::toString).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void loadUserByUsernameThrowsWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@example.com"));
    }
}
