
package com.stockia.api.controller;

import com.stockia.api.dto.request.LoginRequest;
import com.stockia.api.dto.response.LoginResponse;
import com.stockia.api.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> credenciaisInvalidas() {

        return ResponseEntity
                .status(401)
                .body("E-mail ou senha inválidos.");
    }
}
