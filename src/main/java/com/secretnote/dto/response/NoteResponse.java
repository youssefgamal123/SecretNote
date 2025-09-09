package com.secretnote.dto.response;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NoteResponse {

    private Long id;
    private String title;
    private String content;


}
