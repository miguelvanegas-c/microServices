package eci.edu.co.monolito.controller;

import eci.edu.co.monolito.DTO.response.PostDTO;
import eci.edu.co.monolito.response.ApiResponse;
import eci.edu.co.monolito.service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostDTO>> createPost(@Valid @RequestBody PostDTO postDTO){
        PostDTO created = postService.createPost(postDTO);
        ApiResponse<PostDTO> body = buildResponse(created, "Post created", HttpStatus.CREATED).getBody();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDTO>> getPost(@PathVariable String id){
        PostDTO dto = postService.getPostById(id);
        return buildResponse(dto, "Post retrieved", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostDTO>>> getAllPosts(){
        List<PostDTO> list = postService.getAllPosts();
        return buildResponse(list, "Posts retrieved", HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDTO>> updatePost(@PathVariable String id, @Valid @RequestBody PostDTO postDTO){
        PostDTO updated = postService.updatePost(id, postDTO);
        return buildResponse(updated, "Post updated", HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePost(@PathVariable String id){
        postService.deletePost(id);
        return buildResponse(null, "Post deleted", HttpStatus.NO_CONTENT);
    }

    private <T> ResponseEntity<ApiResponse<T>> buildResponse(T data, String message, HttpStatus status) {
        ApiResponse<T> body = ApiResponse.<T>builder()
                .data(data)
                .message(message)
                .code(status.value())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}

