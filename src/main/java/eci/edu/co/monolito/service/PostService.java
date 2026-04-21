package eci.edu.co.monolito.service;

import eci.edu.co.monolito.DTO.response.PostDTO;

import java.util.List;

public interface PostService {
    PostDTO createPost(PostDTO postDTO);
    PostDTO getPostById(String id);
    PostDTO updatePost(String id, PostDTO postDTO);
    void deletePost(String id);
    List<PostDTO> getAllPosts();
}

