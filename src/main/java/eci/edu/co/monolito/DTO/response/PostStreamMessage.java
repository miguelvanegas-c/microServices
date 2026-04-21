package eci.edu.co.monolito.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostStreamMessage {
    private String action; // CREATED, UPDATED, DELETED
    private PostDTO post;
}

