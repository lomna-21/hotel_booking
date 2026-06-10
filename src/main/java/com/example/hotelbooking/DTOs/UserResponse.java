package com.example.hotelbooking.DTOs;

import com.example.hotelbooking.DTOs.Role.RoleResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String username;

    private String email;

    private String publicId;

    private List<RoleResponse> role;
}
