package eci.edu.co.monolito.service.impl;

import eci.edu.co.monolito.DTO.response.PostDTO;
import eci.edu.co.monolito.exception.UserExceptionNotFound;
import eci.edu.co.monolito.model.Post;
import eci.edu.co.monolito.model.User;
import eci.edu.co.monolito.repository.PostRepository;
import eci.edu.co.monolito.repository.UserRepository;
import eci.edu.co.monolito.mapper.PostMapper;
import eci.edu.co.monolito.service.PostService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Override
    @Transactional
    public PostDTO createPost(PostDTO postDTO) {
        User user = userRepository.findById(postDTO.getUserId())
                .orElseThrow(() -> new UserExceptionNotFound("User not found with name " + postDTO.getUserName()));
        Post post = postMapper.toEntity(postDTO);
        post.setId(UUID.randomUUID().toString());
        post.setCreationDate(LocalDateTime.now());
        post.setUser(user);
        Post saved = postRepository.save(post);
        PostDTO dto = postMapper.toDto(saved);
        // broadcast to subscribers
        broadcast("CREATED", dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public PostDTO getPostById(String id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new UserExceptionNotFound("Post not found with id " + id));
        return postMapper.toDto(post);
    }

    @Override
    @Transactional
    public PostDTO updatePost(String id, PostDTO postDTO) {
        Post post = postRepository.findById(id).orElseThrow(() -> new UserExceptionNotFound("Post not found with id " + id));
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        Post updated = postRepository.save(post);
        PostDTO dto = postMapper.toDto(updated);
        broadcast("UPDATED", dto);
        return dto;
    }

    @Override
    @Transactional
    public void deletePost(String id) {
        if (!postRepository.existsById(id)) {
            throw new UserExceptionNotFound("Post not found with id " + id);
        }
        postRepository.deleteById(id);
        // send a message with action DELETED and post with only id set
        eci.edu.co.monolito.DTO.response.PostDTO dto = new eci.edu.co.monolito.DTO.response.PostDTO();
        dto.setId(id);
        broadcast("DELETED", dto);
    }

    private void broadcast(String action, PostDTO dto) {
        try {
            messagingTemplate.convertAndSend("/topic/posts", new eci.edu.co.monolito.DTO.response.PostStreamMessage(action, dto));
            logger.debug("Broadcasted post {} action {}", dto == null ? "null" : dto.getId(), action);
        } catch (Exception ex) {
            logger.error("Failed to broadcast post {} action {}", dto == null ? "null" : dto.getId(), action, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDTO> getAllPosts() {
        return postRepository.findAll().stream().map(postMapper::toDto).collect(Collectors.toList());
    }

    // Mapping handled by MapStruct PostMapper
}

