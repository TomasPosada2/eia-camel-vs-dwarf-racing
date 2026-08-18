package com.eia.racing.service;

import com.eia.racing.dto.auth.AdminCreateUserRequest;
import com.eia.racing.dto.auth.UserProfileResponse;
import com.eia.racing.exception.BusinessRuleViolationException;
import com.eia.racing.exception.DuplicateResourceException;
import com.eia.racing.exception.ResourceNotFoundException;
import com.eia.racing.mapper.UserMapper;
import com.eia.racing.model.User;
import com.eia.racing.model.UserRole;
import com.eia.racing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserProfileResponse createUser(AdminCreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("A user with email " + request.email() + " already exists");
        }

        User user = User.builder()
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(request.role())
                .enabled(true)
                .build();
        user = userRepository.save(user);

        auditService.record("USER_CREATED_BY_ADMIN", "User", user.getId(),
                "Administrator created user '" + user.getEmail() + "' with role " + user.getRole());

        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public void deleteUser(Long id, String currentUsername) {
        User user = findUserOrThrow(id);
        if (user.getEmail().equalsIgnoreCase(currentUsername)) {
            throw new BusinessRuleViolationException("You cannot delete your own account");
        }

        userRepository.delete(user);

        auditService.record("USER_DELETED", "User", id,
                "Administrator deleted user '" + user.getEmail() + "'");
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toProfileResponse);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUser(Long id) {
        return userMapper.toProfileResponse(findUserOrThrow(id));
    }

    @Transactional
    public UserProfileResponse updateRole(Long id, UserRole newRole) {
        User user = findUserOrThrow(id);
        UserRole previousRole = user.getRole();
        user.setRole(newRole);
        user = userRepository.save(user);

        auditService.record("USER_ROLE_CHANGED", "User", user.getId(),
                "Role changed from " + previousRole + " to " + newRole,
                previousRole.name(), newRole.name());

        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateStatus(Long id, boolean enabled) {
        User user = findUserOrThrow(id);
        boolean previous = user.isEnabled();
        user.setEnabled(enabled);
        user = userRepository.save(user);

        auditService.record("USER_STATUS_CHANGED", "User", user.getId(),
                enabled ? "User enabled" : "User disabled",
                String.valueOf(previous), String.valueOf(enabled));

        return userMapper.toProfileResponse(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
