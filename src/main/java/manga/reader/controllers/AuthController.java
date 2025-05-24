package manga.reader.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import manga.reader.exception.AuthenticationException;
import manga.reader.jwt.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Operation(
            summary = "User login",
            description = "Authenticates user with username, password, and role. Returns JWT token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.username() == null || loginRequest.password() == null) {
            throw new AuthenticationException("Username and password are required");
        }

        if (loginRequest.role() == null) {
            throw new AuthenticationException("Role is required");
        }

        String token = jwtUtil.generateToken(loginRequest.username(), loginRequest.role());

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", loginRequest.username());
        response.put("role", loginRequest.role());

        return ResponseEntity.ok(response);
    }

    public record LoginRequest(
            String username,
            String password,
            String role
    ) {}
}