package eci.edu.co.monolito.mapper;

import eci.edu.co.monolito.DTO.response.PostDTO;
import eci.edu.co.monolito.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "userId", source = "user.id")
    PostDTO toDto(Post post);

    // when mapping from DTO to entity we won't set the user (handled in service)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    Post toEntity(PostDTO dto);
}

