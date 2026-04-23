package eci.edu.co.monolito.mapper;

import eci.edu.co.monolito.DTO.request.CreateUserDTO;
import eci.edu.co.monolito.DTO.response.UserDTO;
import eci.edu.co.monolito.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "posts", qualifiedByName = "mapPosts")
    UserDTO toDto(User user);

    @org.mapstruct.Named("mapPosts")
    default List<eci.edu.co.monolito.DTO.response.PostDTO> mapPosts(List<eci.edu.co.monolito.model.Post> posts) {
        if (posts == null) return null;
        return posts.stream().map(post -> {
            eci.edu.co.monolito.DTO.response.PostDTO dto = new eci.edu.co.monolito.DTO.response.PostDTO();
            dto.setId(post.getId());
            dto.setUserName(post.getUser() != null ? post.getUser().getName() : null);
            dto.setTitle(post.getTitle());
            dto.setContent(post.getContent());
            return dto;
        }).toList();
    }

    @Mapping(target = "posts", ignore = true)
    User createDtoToUser(CreateUserDTO dto);
}
