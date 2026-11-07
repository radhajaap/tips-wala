package com.marketpulse.config;
import java.util.Map; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> invalid(MethodArgumentNotValidException e){
    var fe = e.getBindingResult().getFieldError();
    return ResponseEntity.badRequest().body(Map.of("error", fe == null ? "Invalid input" : fe.getField() + ": " + fe.getDefaultMessage())); }
}
