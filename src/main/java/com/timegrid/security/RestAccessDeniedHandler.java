package com.timegrid.security;

import com.timegrid.common.Messages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final Messages messages;

    public RestAccessDeniedHandler(ObjectMapper objectMapper, Messages messages) {
        this.objectMapper = objectMapper;
        this.messages = messages;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        Locale locale = locale(request);

        // 1. Criar o ProblemDetail com o status 403 Forbidden
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);

        // 2. Personalizar campos adicionais (opcional)
        detail.setTitle(messages.get("error.title", locale));
        detail.setDetail(messages.get("auth.forbidden", locale));
        detail.setInstance(URI.create(request.getRequestURI()));

        // 3. Configurar a resposta HTTP
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        // 4. Escrever o objeto no corpo da resposta usando o Jackson
        objectMapper.writeValue(response.getWriter(), detail);
    }

    private Locale locale(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (header == null || header.isBlank()) {
            return Locale.ENGLISH;
        }
        return Locale.forLanguageTag(header.split(",")[0].trim());
    }
}
