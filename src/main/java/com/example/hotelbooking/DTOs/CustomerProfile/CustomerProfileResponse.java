package com.example.hotelbooking.DTOs.CustomerProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerProfileResponse {

    private String publicId;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;
}
