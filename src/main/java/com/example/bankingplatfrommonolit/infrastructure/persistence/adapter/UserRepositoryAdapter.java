package com.example.bankingplatfrommonolit.infrastructure.persistence.adapter;

import com.example.bankingplatfrommonolit.domain.model.User;
import com.example.bankingplatfrommonolit.domain.port.repo.UserRepositoryPort;
import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.UserEntity;
import com.example.bankingplatfrommonolit.infrastructure.persistence.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository jpa;

    @Override
    public boolean existsByUsernameOrEmail(String u, String e) {
        return jpa.existsByUsernameOrEmail(u, e);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String login) {
        return jpa.findByUsernameOrEmail(login).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public User save(User u) {
        return toDomain(jpa.save(toEntity(u)));
    }

    @Override
    @Transactional
    public void updateTokenVersion(UUID userId, int v) {
        jpa.updateTokenVersion(userId, v);
    }

    @Override
    @Transactional
    public void updateActiveStatus(UUID userId, boolean active) {
        jpa.updateActiveStatus(userId, active);
    }

    @Override
    public List<User> findAll(int page, int size) {
        var p = PageRequest.of(page, size);
        return jpa.findAll(p).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private User toDomain(UserEntity e) {
        return User.builder()
                .id(e.getId())
                .username(e.getUsername())
                .email(e.getEmail())
                .passwordHash(e.getPasswordHash())
                .role(e.getRole())
                .active(e.isActive())
                .tokenVersion(e.getTokenVersion())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private UserEntity toEntity(User u) {
        var e = new UserEntity();
        e.setId(u.getId());
        e.setUsername(u.getUsername());
        e.setEmail(u.getEmail());
        e.setPasswordHash(u.getPasswordHash());
        e.setRole(u.getRole());
        e.setActive(u.isActive());
        e.setTokenVersion(u.getTokenVersion());
        e.setCreatedAt(u.getCreatedAt());
        e.setUpdatedAt(u.getUpdatedAt());
        return e;
    }
}
