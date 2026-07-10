package com.timegrid.common;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Messages {

    private final MessageSource messageSource;

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, Locale.getDefault());
    }

    public String get(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

}
