package com.example.Diallock_AI.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Diallock_AI.model.FollowUp;

public interface FollowUpRepository extends JpaRepository<FollowUp, Integer> {
    List<FollowUp> findByFollowUpDate(LocalDate date);
}
