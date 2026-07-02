package com.timegrid.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        // 1. Criar o ProblemDetail com o status 403 Forbidden
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
  
        // 2. Personalizar campos adicionais (opcional)
        detail.setTitle("Forbidden");
        detail.setDetail("You do not have permission to access this resource.");
        detail.setInstance(URI.create(request.getRequestURI()));

        // 3. Configurar a resposta HTTP
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        // 4. Escrever o objeto no corpo da resposta usando o Jackson
        objectMapper.writeValue(response.getWriter(), detail);
    }

}
