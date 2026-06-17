package com.example.hotelbooking.Controllers;


import com.example.hotelbooking.DTOs.AuthRequest;
import com.example.hotelbooking.DTOs.AuthResponse;
import com.example.hotelbooking.DTOs.AuthRegisterRequest;
import com.example.hotelbooking.DTOs.CustomerProfile.CustomerProfileRequest;
import com.example.hotelbooking.Services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/authenticate")
@RequiredArgsConstructor
@Tag(name = "Authorization API's", description = "Operations related to Authentication")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Api for registering as a hotel owner")
    @PostMapping("/register-as-owner")
    public String registerAsOwner(@Valid @RequestBody AuthRegisterRequest request){

        return authService.registerAsOwner(request);
    }

    @Operation(summary = "Api for registering as a customer")
    @PostMapping("/register-as-customer")
    public String registerAsCustomer (@Valid @RequestBody CustomerProfileRequest request){

        System.out.println("Request: "+request);
        return authService.registerAsCustomer(request);
    }

    @Operation(summary = "Api for logging in/generating jwt token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request){

        return authService.login(request);
    }


}
