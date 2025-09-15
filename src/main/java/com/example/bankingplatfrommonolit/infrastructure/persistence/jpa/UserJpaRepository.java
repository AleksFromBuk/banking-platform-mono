package com.example.bankingplatfrommonolit.infrastructure.persistence.jpa;

import com.example.bankingplatfrommonolit.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByUsernameOrEmail(String username, String email);
    @Query("""
           select u
           from UserEntity u
           where u.username = :login or u.email = :login
           """)
    Optional<UserEntity> findByUsernameOrEmail(String login);

    @Modifying
    @Query("UPDATE UserEntity u SET u.tokenVersion = :version WHERE u.id = :id")
    void updateTokenVersion(@Param("id") UUID id, @Param("version") int version);

    @Modifying
    @Query("UPDATE UserEntity u SET u.active = :active WHERE u.id = :id")
    void updateActiveStatus(@Param("id") UUID id, @Param("active") boolean active);
}
