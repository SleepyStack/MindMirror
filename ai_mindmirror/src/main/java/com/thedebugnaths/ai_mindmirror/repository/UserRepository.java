package com.thedebugnaths.ai_mindmirror.repository;

import com.thedebugnaths.ai_mindmirror.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}
