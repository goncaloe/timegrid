package com.timegrid.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

    private final String messageKey;

    public ForbiddenException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

}
