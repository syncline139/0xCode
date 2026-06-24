package com.example.taskservice.repository;

import com.example.taskservice.entity.TaskCriterion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskCriterionRepository extends JpaRepository<TaskCriterion, UUID> {

    List<TaskCriterion> findAllByTaskIdOrderByOrderIndexAsc(UUID taskId);
}
