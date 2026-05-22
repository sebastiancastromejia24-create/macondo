package com.macondo.jewelry.Mapper;

import com.macondo.jewelry.Controller.dto.response.AuthResponse;
import com.macondo.jewelry.Entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "token", source = "token")
    AuthResponse toResponse(AppUser user, String token);
}
