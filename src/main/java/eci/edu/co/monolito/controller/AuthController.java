package eci.edu.co.monolito.controller;

import eci.edu.co.monolito.DTO.response.AuthResponseDTO;
import eci.edu.co.monolito.response.ApiResponse;
import eci.edu.co.monolito.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> me(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        AuthResponseDTO response = authService.getAuthenticatedUser(authorization);
        ApiResponse<AuthResponseDTO> body = ApiResponse.<AuthResponseDTO>builder()
                .data(response)
                .message("Authenticated user")
                .code(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(body);
    }
}

