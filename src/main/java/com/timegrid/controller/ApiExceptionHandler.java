package com.timegrid.controller;

import com.timegrid.exception.BusinessRuleException;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.ForbiddenException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.exception.UnauthorizedException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private final MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex, Locale locale) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessageKey(), ex.getArgs(), locale, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, Locale locale) {
        return problem(HttpStatus.CONFLICT, ex.getMessageKey(), ex.getArgs(), locale, null);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex, Locale locale) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessageKey(), ex.getArgs(), locale, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex, Locale locale) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessageKey(), null, locale, null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex, Locale locale) {
        return problem(HttpStatus.FORBIDDEN, ex.getMessageKey(), null, locale, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, Locale locale) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            String message = error instanceof DefaultMessageSourceResolvable resolvable
                    ? messageSource.getMessage(resolvable, locale)
                    : error.getDefaultMessage();
            errors.putIfAbsent(field, message);
        }
        return problem(HttpStatus.BAD_REQUEST, "validation.failed", null, locale, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex, Locale locale) {
        return problem(HttpStatus.BAD_REQUEST, "malformed.request", null, locale, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, Locale locale) {
        return problem(HttpStatus.BAD_REQUEST, "invalid.parameter", null, locale, null);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ProblemDetail handleInvalidEnum(InvalidFormatException ex, Locale locale) {
        return problem(HttpStatus.BAD_REQUEST, "invalid.parameter", null, locale, null);
    }

    private ProblemDetail problem(HttpStatus status, String messageKey, Object[] args, Locale locale,
            Map<String, String> errors) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status,
                messageSource.getMessage(messageKey, args, locale));
        detail.setTitle(messageSource.getMessage("error.title", null, locale));
        detail.setInstance(URI.create("/api"));
        if (errors != null && !errors.isEmpty()) {
            detail.setProperty("errors", errors);
        }
        return detail;
    }

}
