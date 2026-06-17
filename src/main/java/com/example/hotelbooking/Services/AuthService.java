package com.example.hotelbooking.Services;


import com.example.hotelbooking.DTOs.AuthResponse;
import com.example.hotelbooking.DTOs.AuthRegisterRequest;
import com.example.hotelbooking.DTOs.AuthRequest;
import com.example.hotelbooking.DTOs.CustomerProfile.CustomerProfileRequest;
import com.example.hotelbooking.DTOs.Role.RoleResponse;
import com.example.hotelbooking.DTOs.UserResponse;
import com.example.hotelbooking.DTOs.User.UserDto;
import com.example.hotelbooking.Models.CustomerProfile;
import com.example.hotelbooking.Models.Role;
import com.example.hotelbooking.Respositories.CustomerProfileRepository;
import com.example.hotelbooking.Respositories.RoleRepository;
import com.example.hotelbooking.Respositories.UserRepository;
import com.example.hotelbooking.Models.Permission;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.example.hotelbooking.Models.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.transaction.TransactionScoped;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @Transactional
    public String registerAsOwner(AuthRegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername()) ||
                userRepository.existsByEmail(request.getEmail())) {
            return "Username or Email already registered";
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        try {
            Role role = roleRepository.findByName("OWNER")
                    .orElseThrow(() -> new RuntimeException("Role OWNER not found"));

            user.setRoles(Collections.singleton(role));
            userRepository.save(user);
            return "Owner registered successfully";
        } catch (Exception ex) {
            throw new RuntimeException("Unable to process request. Contact Admin");
        }
    }

    @Transactional
    public String registerAsCustomer(@Valid @RequestBody CustomerProfileRequest request) {

        if (userRepository.existsByUsername(request.getUsername()) ||
                userRepository.existsByEmail(request.getEmail())) {
            return "Username or Email already registered";
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();
        try {
            Role role = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("Role CUSTOMER not found"));

            user.setRoles(Collections.singleton(role));
            userRepository.save(user);

            if (customerProfileRepository.existsByEmail(request.getEmail())) {
                return "Email already registered";
            } else if (customerProfileRepository.existsByPhone(request.getPhone())) {
                return "Phone already registered";
            }
            CustomerProfile customerProfile = CustomerProfile.builder()
                    .firstName(request.getFirstName()).lastName(request.getLastName())
                    .email(request.getEmail()).phone(request.getPhone())
                    .user(user).build();
            customerProfileRepository.save(customerProfile);
            return "Customer registered successfully";
        }catch (Exception ex){
            throw new RuntimeException("Unexpected Error Occurred");
        }
    }

    public ResponseEntity<AuthResponse> login(AuthRequest request){

        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword()
            ));
        }catch (Exception ex){
            throw new BadCredentialsException("Invalid username password provided");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + request.getUsername()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String token = jwtUtil.generateToken(userDetails);

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        UserDto userDto = UserDto.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .permissions(permissions)
                .build();

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(userDto)
                .build());

    }
}
