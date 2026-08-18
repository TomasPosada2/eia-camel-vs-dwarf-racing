package com.eia.racing.mapper;

import com.eia.racing.dto.auth.UserProfileResponse;
import com.eia.racing.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt());
    }
}
