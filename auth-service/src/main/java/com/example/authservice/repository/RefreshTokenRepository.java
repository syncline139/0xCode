package com.example.authservice.repository;

import com.example.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findByUserId(UUID userId);

    Optional<RefreshToken> findByToken(String token);

    void deleteAllByUserId(UUID userId);

    void deleteByUserIdAndToken(UUID userId, String token);
}
