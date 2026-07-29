package com.timegrid.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.timegrid.config.MessageConfig;
import com.timegrid.dto.AuthResponse;
import com.timegrid.dto.LogoutRequest;
import com.timegrid.exception.UnauthorizedException;
import com.timegrid.security.AppUserDetailsService;
import com.timegrid.security.JwtService;
import com.timegrid.service.AuthService;

// addFilters = false: a cadeia de segurança real (SecurityConfig, JwtAuthenticationFilter) não é o alvo deste
// teste, só o mapeamento do controller e o ApiExceptionHandler
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MessageConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // não usados diretamente no teste, mas necessários para instanciar o JwtAuthenticationFilter
    // (é um @Component normal, o WebMvcTest apanha-o na slice mesmo com addFilters = false)
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    void loginComCredenciaisValidasDevolveTokens() throws Exception {
        AuthResponse response = new AuthResponse(UUID.randomUUID(), "goncalo.esteves", "Gonçalo Esteves",
                UUID.randomUUID(), "Tejo Digital", List.of("ADMIN"), List.of("user:write"),
                "access-token", "refresh-token", Instant.now().plusSeconds(900));
        given(authService.login(any())).willReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("""
                        {"username":"goncalo.esteves","password":"timegrid2026"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void loginSemPasswordDevolveProblemDetailComErrosDeValidacao() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("""
                        {"username":"goncalo.esteves","password":""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.errors.password", notNullValue()));
    }

    @Test
    void loginComCredenciaisErradasDevolve401() throws Exception {
        given(authService.login(any())).willThrow(new UnauthorizedException("user.credentials.invalid"));

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("""
                        {"username":"goncalo.esteves","password":"errada"}
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password."));
    }

    @Test
    void logoutDevolve204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .contentType("application/json")
                .content("""
                        {"refreshToken":"algum-token"}
                        """))
                .andExpect(status().isNoContent());

        verify(authService).logout(new LogoutRequest("algum-token"));
    }

}