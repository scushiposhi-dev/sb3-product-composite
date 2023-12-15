package com.example.sb3productcomposite.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EventProcessingException extends RuntimeException{
    public EventProcessingException(String message) {
        super(message);
    }

    public EventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventProcessingException(Throwable cause) {
        super(cause);
    }
}
