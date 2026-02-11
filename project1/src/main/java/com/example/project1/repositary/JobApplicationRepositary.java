package com.example.project1.repositary;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project1.module.JobApplication;

@Repository
public interface JobApplicationRepositary extends JpaRepository<JobApplication,Long> {
    List<JobApplication> findByUserId(Long userId);
    List<JobApplication> findByJobId(Long jobId);
}
