package org.example.backend.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.user.dto.request.UpdateProfileRequest;
import org.example.backend.domain.user.dto.response.UserProfileResponse;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.service.UserService;
import org.example.backend.global.jwt.custom.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal){

        User user = userService.getCurrentUser(principal.getUser().getId());
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getLoginType(),
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody UpdateProfileRequest request) {

        User user = userService.updateNickname(principal.getUser().getId(), request.nickname());
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getLoginType(),
                user.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal CustomUserDetails principal) {
        userService.deleteUser(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }

}
