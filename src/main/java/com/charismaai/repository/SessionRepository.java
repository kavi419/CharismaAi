package com.charismaai.repository;

import com.charismaai.model.PracticeSession;
import com.charismaai.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<PracticeSession, Long> {
    List<PracticeSession> findByUser(User user, Sort sort);

    List<PracticeSession> findByUserOrderByCreatedDateDesc(User user);
}
