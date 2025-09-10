package com.secretnote.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class APIExceptionResponse extends RuntimeException  {

    private String message;
    private  HttpStatus  status;



    public APIExceptionResponse(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public APIExceptionResponse(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public APIExceptionResponse(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }


}
