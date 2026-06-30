package com.timegrid.exception;

import lombok.Getter;

@Getter
public class BusinessRuleException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;

    public BusinessRuleException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }

}
