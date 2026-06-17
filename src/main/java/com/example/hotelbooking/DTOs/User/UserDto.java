package com.example.hotelbooking.DTOs.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
        private String publicId;
        private String username;
        private String email;
        private Set<String> roles;
        private Set<String> permissions;
    }

