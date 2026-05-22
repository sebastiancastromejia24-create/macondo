package com.macondo.jewelry.Controller;


import com.macondo.jewelry.Controller.Dtos.AuthDtos;
import com.macondo.jewelry.Service.AuthService;
import com.macondo.jewelry.Controller.Dtos.AuthDtos.AuthResponse;
import com.macondo.jewelry.Controller.Dtos.AuthDtos.LoginRequest;
import com.macondo.jewelry.Controller.Dtos.AuthDtos.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
