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
    
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        if (userRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
            throw new UserExceptionBadRequest("Email already registered: " + createUserDTO.getEmail());
        }

        User user = userMapper.createDtoToUser(createUserDTO);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserExceptionNotFound("User not found with id " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptionNotFound("User not found with email " + email));
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


