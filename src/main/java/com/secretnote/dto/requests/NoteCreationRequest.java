package com.secretnote.dto.requests;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class NoteCreationRequest {


    private String title;
    private String content;

    boolean selfDestructive;
    LocalDateTime expiresAt;

}
