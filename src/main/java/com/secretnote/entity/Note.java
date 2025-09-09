package com.secretnote.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;


    private String title;

    @Column(length = 5000)
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    private boolean selfDestructive;

    private LocalDateTime expiresAt;



}
