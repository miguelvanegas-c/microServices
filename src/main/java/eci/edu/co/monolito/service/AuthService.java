package eci.edu.co.monolito.service;

import eci.edu.co.monolito.DTO.response.AuthResponseDTO;

public interface AuthService {
    /**
     * Build an AuthResponse based on the current authenticated principal (extracted from SecurityContext)
     * and the raw Authorization header token provided by the caller.
     */
    AuthResponseDTO getAuthenticatedUser(String authorizationHeader);
}

