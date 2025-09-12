package com.example.bankingplatfrommonolit.domain.port.repo;

import com.example.bankingplatfrommonolit.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    boolean existsByUsernameOrEmail(String username, String email);
    Optional<User> findByUsernameOrEmail(String login);
    Optional<User> findById(UUID id);
    User save(User user);
    void updateTokenVersion(UUID userId, int newVersion);
    List<User> findAll(int page, int size);
    void updateActiveStatus(UUID userId, boolean active);
}
