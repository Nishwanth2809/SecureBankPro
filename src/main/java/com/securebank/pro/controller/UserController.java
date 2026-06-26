package com.securebank.pro.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securebank.pro.dto.request.RegisterRequestDTO;
import com.securebank.pro.dto.response.ApiResponseDTO;
import com.securebank.pro.entity.User;
import com.securebank.pro.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO> registerUser(@RequestBody RegisterRequestDTO request) {
        try {
            User user = new User(request.getFullName(), request.getEmail(), request.getPassword());
            userService.addUser(user);
            return ResponseEntity.ok(new ApiResponseDTO(true, "User registered successfully with ID " + user.getUserId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchUser(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> removeUser(@PathVariable int id) {
        try {
            userService.removeUser(id);
            return ResponseEntity.ok(new ApiResponseDTO(true, "User with ID " + id + " was successfully removed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }
}
