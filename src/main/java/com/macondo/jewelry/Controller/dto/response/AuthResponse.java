package com.macondo.jewelry.Controller.dto.response;

import com.macondo.jewelry.Entity.UserRole;

public record AuthResponse(Long id, String name, String email, UserRole role, String token) {
}
