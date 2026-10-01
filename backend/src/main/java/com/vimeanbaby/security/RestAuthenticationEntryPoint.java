package com.vimeanbaby.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vimeanbaby.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Writes 401 / 403 responses in the standard {@link ApiResponse} shape. */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

    public static final String TOKEN_ERROR_ATTRIBUTE = "vb.tokenError";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        String code = request.getAttribute(TOKEN_ERROR_ATTRIBUTE) != null ? "TOKEN_INVALID" : "UNAUTHORIZED";
        write(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required", code);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN, "Access denied", "FORBIDDEN");
    }

    private void write(HttpServletResponse response, int status, String message, String code) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.fail(message, Map.of("code", code)));
    }
}
