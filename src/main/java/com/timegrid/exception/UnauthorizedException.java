package com.timegrid.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final String messageKey;

    public UnauthorizedException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

}
