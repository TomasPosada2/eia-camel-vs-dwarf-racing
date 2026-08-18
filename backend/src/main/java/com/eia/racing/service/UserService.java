package com.eia.racing.service;

import com.eia.racing.dto.auth.UserProfileResponse;
import com.eia.racing.exception.ResourceNotFoundException;
import com.eia.racing.mapper.UserMapper;
import com.eia.racing.model.User;
import com.eia.racing.model.UserRole;
import com.eia.racing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditService auditService;

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
