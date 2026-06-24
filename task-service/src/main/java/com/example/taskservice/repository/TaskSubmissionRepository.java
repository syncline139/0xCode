package com.example.taskservice.repository;

import com.example.taskservice.entity.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, UUID> {

    List<TaskSubmission> findAllByTaskId(UUID taskId);

    List<TaskSubmission> findAllByUserId(UUID userId);
}
