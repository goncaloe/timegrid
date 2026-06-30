package com.timegrid.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public NotFoundException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

}
