package eci.edu.co.monolito.controller;

import eci.edu.co.monolito.DTO.request.CreateUserDTO;
import eci.edu.co.monolito.DTO.response.UserDTO;
import eci.edu.co.monolito.response.ApiResponse;
import eci.edu.co.monolito.service.UserService;
import lombok.AllArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Validated
public class UserController {

	private final UserService userService;

	@PostMapping
	public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
		UserDTO created = userService.createUser(createUserDTO);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(created.getId()).toUri();
		ApiResponse<UserDTO> body = buildResponse(created, "User created", HttpStatus.CREATED).getBody();
		return ResponseEntity.created(location).body(body);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable Long id) {
		UserDTO dto = userService.getUserById(id);
		return buildResponse(dto, "User retrieved", HttpStatus.OK);
	}

	@GetMapping("/email")
	public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(
			@RequestParam @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email
	) {
		UserDTO dto = userService.getUserByEmail(email);
		return buildResponse(dto, "User retrieved", HttpStatus.OK);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
		UserDTO updated = userService.updateUser(id, userDTO);
		return buildResponse(updated, "User updated", HttpStatus.OK);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return buildResponse(null, "User deleted", HttpStatus.NO_CONTENT);
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
