package eci.edu.co.monolito.service;

import eci.edu.co.monolito.DTO.request.CreateUserDTO;
import eci.edu.co.monolito.DTO.response.UserDTO;

public interface UserService {
    UserDTO createUser(CreateUserDTO createUserDTO);
    UserDTO getUserById(Long id);
    UserDTO getUserByEmail(String email);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
}

