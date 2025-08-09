package com.security.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {

    private String error;

    private String details;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "hh:mm:ss a")
    private LocalDateTime timestamp;

}
