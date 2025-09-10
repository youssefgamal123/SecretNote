package com.secretnote.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@AllArgsConstructor
public class APIExceptionResponse {

    private String message;
    private  HttpStatus  status;


}
