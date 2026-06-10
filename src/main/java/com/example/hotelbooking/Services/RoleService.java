package com.example.hotelbooking.Services;


import com.example.hotelbooking.DTOs.Role.RoleRequest;
import com.example.hotelbooking.DTOs.Role.RoleResponse;
import com.example.hotelbooking.Models.Role;
import com.example.hotelbooking.Respositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public String create(RoleRequest request)  {

        Role role = Role.builder().name(request.getName().toUpperCase()).build();
        try{
            Role savedRole = roleRepository.save(role);
            return ("Role "+savedRole.getName()+" added");
        }catch (Exception ex){
            throw new RuntimeException("Role could not be added"+ex.getMessage());
        }
    }

    public List<RoleResponse> getAllRoles(){

        List<Role> roles = roleRepository.findAll();

        List<RoleResponse> responses = new ArrayList<>();

        for(Role role : roles){
            RoleResponse response = RoleResponse.builder().name(role.getName()).build();
            responses.add(response);
        }
        return responses;
    }
}
