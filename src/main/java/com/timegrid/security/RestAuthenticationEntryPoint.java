package com.timegrid.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        // 1. Criar o ProblemDetail com o status 401 Unauthorized
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        
        // 2. Personalizar campos adicionais
        detail.setTitle("Error");
        detail.setDetail("You do not have permission to access this resource.");
        detail.setType(URI.create("https://api.seusite.com/errors/unauthorized"));
        detail.setInstance(URI.create(request.getRequestURI()));

        // 3. Configurar a resposta HTTP
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        // 4. Escrever o objeto no corpo da resposta usando o Jackson
        objectMapper.writeValue(response.getWriter(), detail);

    }

}
