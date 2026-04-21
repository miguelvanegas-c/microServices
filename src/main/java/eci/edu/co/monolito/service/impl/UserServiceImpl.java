package eci.edu.co.monolito.service.impl;

import eci.edu.co.monolito.DTO.request.CreateUserDTO;
import eci.edu.co.monolito.DTO.response.UserDTO;
import eci.edu.co.monolito.exception.UserExceptionNotFound;
import eci.edu.co.monolito.exception.UserExceptionBadRequest;
import eci.edu.co.monolito.model.User;
import eci.edu.co.monolito.repository.UserRepository;
import eci.edu.co.monolito.mapper.UserMapper;
import eci.edu.co.monolito.service.UserService;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDTO createUserFromJwt(CreateUserDTO createUserDTO) {
        var authentication = getAuthentication();
        JwtClaims claims = extractClaims(authentication);
        User existing = findExistingUser(claims.email(), claims.auth0Id());
        if (existing != null) {
            return userMapper.toDto(existing);
        }
        validateEmailVerified(claims.emailVerified());
        User user = buildUserFromDtoAndClaims(createUserDTO, claims);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    private org.springframework.security.core.Authentication getAuthentication() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserExceptionBadRequest("Not authenticated");
        }
        return authentication;
    }

    private record JwtClaims(String email, String auth0Id, String name, Boolean emailVerified) {}

    private JwtClaims extractClaims(org.springframework.security.core.Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            String auth0Id = jwt.getSubject();
            String name = jwt.getClaimAsString("name");
            Boolean emailVerified = null;
            Object ev = jwt.getClaims().get("email_verified");
            if (ev instanceof Boolean) {
                emailVerified = (Boolean) ev;
            }
            if (email == null) {
                throw new UserExceptionBadRequest("Email claim missing in token");
            }
            return new JwtClaims(email, auth0Id, name, emailVerified);
        }
        throw new UserExceptionBadRequest("Unsupported authentication principal type");
    }

    private User findExistingUser(String email, String auth0Id) {
        if (auth0Id != null && !auth0Id.isBlank()) {
            var byAuth0 = userRepository.findByAuth0Id(auth0Id).orElse(null);
            if (byAuth0 != null) return byAuth0;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    private void validateEmailVerified(Boolean emailVerified) {
        if (emailVerified != null && !emailVerified) {
            throw new UserExceptionBadRequest("Email not verified");
        }
    }

    private User buildUserFromDtoAndClaims(CreateUserDTO createUserDTO, JwtClaims claims) {
        User user = userMapper.createDtoToUser(createUserDTO);
        user.setEmail(claims.email());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(claims.name());
        }
        user.setPassword(null);
        user.setAuth0Id(claims.auth0Id());
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserExceptionNotFound("User not found with id " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserExceptionNotFound("User not found with id " + id));
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        // password is not present in UserDTO; if you want to update password include it
        User updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserExceptionNotFound("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }

    // Mapping handled by MapStruct UserMapper
}


