package manga.reader.controllers;

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
        String role = loginRequest.role();

        if (role != null) {
            String token = jwtUtil.generateToken(loginRequest.username(), role);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", loginRequest.username());
            response.put("role", role);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    public record LoginRequest (
        String username,
        String password,
        String role
    ) {}
}