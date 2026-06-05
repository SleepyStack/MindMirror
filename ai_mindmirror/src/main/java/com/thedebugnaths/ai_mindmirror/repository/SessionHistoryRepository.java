package com.thedebugnaths.ai_mindmirror.repository;

import com.thedebugnaths.ai_mindmirror.entity.SessionHistory;
import com.thedebugnaths.ai_mindmirror.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionHistoryRepository extends JpaRepository<SessionHistory, Long> {

    List<SessionHistory> findAllByUserOrderByCreatedAtDesc(User user);
    List<SessionHistory> findTop3ByUserOrderByCreatedAtDesc(User user);
}