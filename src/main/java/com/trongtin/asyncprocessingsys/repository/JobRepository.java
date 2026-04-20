package com.trongtin.asyncprocessingsys.repository;

import com.trongtin.asyncprocessingsys.model.Job;
import com.trongtin.asyncprocessingsys.model.enums.JobStatus;
import com.trongtin.asyncprocessingsys.model.enums.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

// repository/JobRepository.java
@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    // Tìm job theo status — dùng sau này cho scheduler
    List<Job> findByStatus(JobStatus status);

    // Tìm job theo type và status
    List<Job> findByTypeAndStatus(JobType type, JobStatus status);
}