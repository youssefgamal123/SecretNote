package com.secretnote.service;


import com.secretnote.dto.RegisterRequest;
import com.secretnote.entity.User;
import com.secretnote.enums.Role;
import com.secretnote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;



    public User register(RegisterRequest registerRequest) {
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email address already in use");
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .roles(Set.of(Role.ROLE_USER))
                .build();

        return userRepository.save(user);
    }









}
