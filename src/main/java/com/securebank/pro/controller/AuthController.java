package com.securebank.pro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securebank.pro.dto.mapper.DtoMapper;
import com.securebank.pro.dto.request.LoginRequestDTO;
import com.securebank.pro.dto.request.RegisterRequestDTO;
import com.securebank.pro.dto.response.ApiResponseDTO;
import com.securebank.pro.dto.response.AuthResponseDTO;
import com.securebank.pro.entity.User;
import com.securebank.pro.service.AuthService;
import com.securebank.pro.service.UserService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, login, logout, and session management. " +
        "Login returns a JWT token required for all protected endpoints.")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final DtoMapper dtoMapper;

    public AuthController(AuthService authService, UserService userService, DtoMapper dtoMapper) {
        this.authService = authService;
        this.userService = userService;
        this.dtoMapper = dtoMapper;
    }

    // ── REGISTER ─────────────────────────────────────────────────────────────

    @Operation(
        summary = "Register a new user",
        description = "Creates a new CUSTOMER account. The password is BCrypt-encrypted before storage."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User registered successfully",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"success": true, "message": "User registered successfully with ID 4"}
                    """))),
        @ApiResponse(responseCode = "400", description = "Validation error or email already exists",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"success": false, "message": "Email already registered: john@example.com"}
                    """)))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO> register(
            @RequestBody(
                description = "User registration details",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterRequestDTO.class),
                    examples = @ExampleObject(
                        name = "Sample Request",
                        value = """
                            {
                              "fullName": "John Doe",
                              "email": "john@example.com",
                              "password": "SecurePass123"
                            }
                            """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody RegisterRequestDTO request) {
        try {
            User user = dtoMapper.convertDTOToEntity(request);
            userService.addUser(user);
            return ResponseEntity.ok(new ApiResponseDTO(true, "User registered successfully with ID " + user.getUserId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO(false, e.getMessage()));
        }
    }

    // ── LOGIN ────────────────────────────────────────────────────────────────

    @Operation(
        summary = "Login and obtain JWT token",
        description = "Authenticates a user and returns a signed JWT token. " +
                "Use this token in the **Authorize** dialog (🔒) at the top of this page to access protected endpoints."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful — JWT token returned",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "email": "admin@securebank.com",
                      "success": true,
                      "message": "Login successful",
                      "token": "eyJhbGciOiJIUzI1NiJ9..."
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "Invalid credentials",
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"email": "wrong@example.com", "success": false, "message": "Invalid credentials", "token": null}
                    """)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody(
                description = "Login credentials",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequestDTO.class),
                    examples = {
                        @ExampleObject(name = "Admin Login",
                            value = """
                                {"email": "admin@securebank.com", "password": "Admin@123"}
                                """),
                        @ExampleObject(name = "Customer Login",
                            value = """
                                {"email": "nishant@example.com", "password": "Pass@123"}
                                """)
                    }
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody LoginRequestDTO request) {
        String token = authService.authenticateUser(request.getEmail(), request.getPassword());
        if (token != null) {
            return ResponseEntity.ok(new AuthResponseDTO(request.getEmail(), true, "Login successful", token));
        } else {
            return ResponseEntity.badRequest().body(new AuthResponseDTO(request.getEmail(), false, "Invalid credentials", null));
        }
    }

    // ── LOGOUT ───────────────────────────────────────────────────────────────

    @Operation(
        summary = "Logout a user",
        description = "Marks the user's session as inactive. The JWT token itself is stateless and remains valid until expiry.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "400", description = "User not found")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO> logout(@RequestParam String email) {
        authService.logout(email);
        return ResponseEntity.ok(new ApiResponseDTO(true, "Logout successful"));
    }

    // ── SESSION CHECK ─────────────────────────────────────────────────────────

    @Operation(
        summary = "Check if a session is active",
        description = "Returns whether the user is currently logged in.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Session status returned")
    @GetMapping("/session")
    public ResponseEntity<AuthResponseDTO> checkSession(@RequestParam String email) {
        boolean active = authService.isSessionActive(email);
        if (active) {
            return ResponseEntity.ok(new AuthResponseDTO(email, true, "Session is active"));
        } else {
            return ResponseEntity.ok(new AuthResponseDTO(email, false, "Session is inactive"));
        }
    }
}
