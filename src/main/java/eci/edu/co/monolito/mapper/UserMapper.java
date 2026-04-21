package eci.edu.co.monolito.mapper;

import eci.edu.co.monolito.DTO.request.CreateUserDTO;
import eci.edu.co.monolito.DTO.response.UserDTO;
import eci.edu.co.monolito.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);

    @Mapping(target = "posts", ignore = true)
    User createDtoToUser(CreateUserDTO dto);
}

