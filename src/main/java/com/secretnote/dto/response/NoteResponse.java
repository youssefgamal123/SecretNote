package com.secretnote.dto.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class NoteResponse {

    private Long id;
    private String title;
    private String content;


}
