package com.example.hotelbooking.DTOs.Manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerResponse {
    private String publicId;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}
