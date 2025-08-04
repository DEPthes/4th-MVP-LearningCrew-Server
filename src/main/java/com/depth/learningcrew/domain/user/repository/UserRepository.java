package com.depth.learningcrew.domain.user.repository;

import com.depth.learningcrew.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String Email);
    Optional<User> findByNickname(String nickname);
    Optional<User> findById(Long id);
    boolean existsByEmail(String id);
    boolean existsByNickname(String nickname);
}
