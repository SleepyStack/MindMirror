package com.thedebugnaths.ai_mindmirror.repository;

import com.thedebugnaths.ai_mindmirror.entity.SessionHistory;
import com.thedebugnaths.ai_mindmirror.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionHistoryRepository extends JpaRepository<SessionHistory, Long> {

    List<SessionHistory> findAllByUserOrderByCreatedAtDesc(User user);
    List<SessionHistory> findTop3ByUserOrderByCreatedAtDesc(User user);
    Optional<SessionHistory> findFirstByUserAndStatusOrderByIdDesc(User user, String status);
    List<SessionHistory> findTop3ByUserAndStatusOrderByCreatedAtDesc(User user, String status);
}