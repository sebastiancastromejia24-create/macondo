package com.macondo.jewelry.Service;

import com.macondo.jewelry.Controller.dto.request.LoginRequest;
import com.macondo.jewelry.Controller.dto.request.RegisterRequest;
import com.macondo.jewelry.Controller.dto.response.AuthResponse;
import com.macondo.jewelry.Mapper.AuthMapper;
import com.macondo.jewelry.Common.BusinessException;
import com.macondo.jewelry.Common.ResourceNotFoundException;
import com.macondo.jewelry.Security.JwtService;
import com.macondo.jewelry.Entity.AppUser;
import com.macondo.jewelry.Repository.UserRepository;
import com.macondo.jewelry.Entity.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuthMapper authMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "El correo ya existe");
        }
        AppUser user = userRepository.save(new AppUser(
                request.name(),
                email,
                passwordEncoder.encode(request.password()),
                UserRole.CLIENTE
        ));
        return authMapper.toResponse(user, jwtService.createToken(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return authMapper.toResponse(user, jwtService.createToken(user));
    }
}
