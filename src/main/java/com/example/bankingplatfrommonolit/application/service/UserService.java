package com.example.bankingplatfrommonolit.application.service;

import com.example.bankingplatfrommonolit.application.dto.user.UserDtos;
import com.example.bankingplatfrommonolit.domain.model.User;
import com.example.bankingplatfrommonolit.domain.port.repo.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepositoryPort userRepository;

    @Transactional(readOnly = true)
    public List<UserDtos.UserResponse> getAllUsers(int page, int size) {
        return userRepository.findAll(page, size).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserStatus(UUID userId, boolean active) {
        userRepository.updateActiveStatus(userId, active);
    }

    private UserDtos.UserResponse toResponse(User user) {
        return new UserDtos.UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isActive(),
                user.getRole()
        );
    }
}
