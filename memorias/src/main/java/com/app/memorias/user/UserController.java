package com.app.memorias.user;

import com.app.memorias.auth.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/me")
  public UserResponse me(@AuthenticationPrincipal AppUser user) {
    return new UserResponse(user.getId().toString(), user.getEmail(), user.getUsername());
  }

  @DeleteMapping("/me")
  public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal AppUser user) {
    userRepository.deleteById(user.getId());
    return ResponseEntity.noContent().build();
  }
}