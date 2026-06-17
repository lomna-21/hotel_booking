package com.example.hotelbooking.DTOs;

import com.example.hotelbooking.DTOs.User.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private UserDto user;
}
