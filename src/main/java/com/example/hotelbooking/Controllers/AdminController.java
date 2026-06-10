package com.example.hotelbooking.Controllers;


import com.example.hotelbooking.DTOs.Role.RoleRequest;
import com.example.hotelbooking.DTOs.Role.RoleResponse;
import com.example.hotelbooking.DTOs.UserResponse;
import com.example.hotelbooking.Services.RoleService;
import com.example.hotelbooking.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin API's", description = "Operations for managing system configurations, users, and administrative tasks")
public class AdminController {

    private final RoleService roleService;
    private final UserService userService;


    @Operation(summary = "Api's for viewing all available roles")
    @GetMapping("/roles")
    public List<RoleResponse> getAllRoles(){

        return roleService.getAllRoles();
    }

    @Operation(summary = "Api's for creating new roles")
    @PostMapping("/roles")
    public String create (@Valid @RequestBody RoleRequest request){

        return roleService.create(request);
    }

    @Operation(summary = "Api's for viewing all available users along with roles")
    @GetMapping("/users-with-role")
    public List<UserResponse> getAllUsersWithRoles(){
        return userService.getAllUsersWithRoles();
    }
}
