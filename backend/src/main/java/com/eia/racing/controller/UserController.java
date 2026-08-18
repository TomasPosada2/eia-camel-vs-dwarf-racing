package com.eia.racing.controller;

import com.eia.racing.dto.auth.AdminCreateUserRequest;
import com.eia.racing.dto.auth.UpdateUserRoleRequest;
import com.eia.racing.dto.auth.UpdateUserStatusRequest;
import com.eia.racing.dto.auth.UserProfileResponse;
import com.eia.racing.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/** Admin-only user management, required by the "Administrator: Manage users" permission. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserProfileResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        userService.deleteUser(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<UserProfileResponse>> listUsers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserProfileResponse> updateRole(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request.role()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserProfileResponse> updateStatus(@PathVariable Long id,
                                                              @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(userService.updateStatus(id, request.enabled()));
    }
}
