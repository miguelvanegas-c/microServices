package eci.edu.co.monolito.exception;

import eci.edu.co.monolito.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserExceptionNotFound.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(UserExceptionNotFound ex){
        return buildResponse(null, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserExceptionBadRequest.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(UserExceptionBadRequest ex){
        return buildResponse(null, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (msg1, msg2) -> msg1 + "; " + msg2
                ));
        return buildResponse(errors, "Validation failed for request body", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (msg1, msg2) -> msg1 + "; " + msg2
                ));
        return buildResponse(errors, "Validation failed for request parameters or form data", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        cv -> cv.getPropertyPath().toString(),
                        cv -> cv.getMessage(),
                        (m1, m2) -> m1 + "; " + m2
                ));
        return buildResponse(errors, "Validation failed for request", HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(Object data, String message, HttpStatus status) {
        ApiResponse<Object> body = ApiResponse.builder()
                .data(data)
                .message(message)
                .code(status.value())
                .build();
        return ResponseEntity.status(status).body(body);
    }

}

