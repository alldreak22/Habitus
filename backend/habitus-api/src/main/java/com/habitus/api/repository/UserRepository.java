package com.habitus.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habitus.api.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNickIgnoreCase(String nick);

    Optional<User> findByEmailIgnoreCase(String email);
}
