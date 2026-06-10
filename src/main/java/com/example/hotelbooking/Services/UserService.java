package com.example.hotelbooking.Services;

import com.example.hotelbooking.DTOs.Role.RoleResponse;
import com.example.hotelbooking.DTOs.UserResponse;
import com.example.hotelbooking.Models.User;
import com.example.hotelbooking.Respositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAllUsersWithRoles(){

        List<User> users = userRepository.findAllUsersWithRoles();

        List<UserResponse> userResponses = new ArrayList<>();
        for(User user: users){
            List<RoleResponse> roleResponses = user.getRoles().
                    stream().map(role -> new RoleResponse(role.getName())).collect(Collectors.toList());
            UserResponse response = UserResponse.builder().
            username(user.getUsername()).email(user.getEmail()).role(roleResponses).build();
            userResponses.add(response);
        }
        return userResponses;
    }
}
