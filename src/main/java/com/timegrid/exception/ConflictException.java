package com.timegrid.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public ConflictException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

}
