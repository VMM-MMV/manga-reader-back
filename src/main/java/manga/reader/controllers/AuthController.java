package manga.reader.controllers;

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