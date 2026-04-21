package eci.edu.co.monolito.service.impl;

import eci.edu.co.monolito.DTO.response.AuthResponseDTO;
import eci.edu.co.monolito.DTO.response.UserDTO;
import eci.edu.co.monolito.exception.UserExceptionBadRequest;
import eci.edu.co.monolito.model.User;
import eci.edu.co.monolito.repository.UserRepository;
import eci.edu.co.monolito.service.AuthService;
import eci.edu.co.monolito.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO getAuthenticatedUser(String authorizationHeader) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserExceptionBadRequest("Not authenticated");
        }
        String principalName = authentication.getName();
        Object principal = authentication.getPrincipal();
        String email = null;
        if (principal instanceof Jwt jwt) {
            Object emailClaim = jwt.getClaims().get("email");
            if (emailClaim != null) {
                email = emailClaim.toString();
            }
        }
        if (email == null) {
            email = principalName;
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptionBadRequest("User not found in local database"));
        UserDTO userDTO = userService.getUserById(user.getId());
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        return new AuthResponseDTO(token, userDTO);
    }
}



