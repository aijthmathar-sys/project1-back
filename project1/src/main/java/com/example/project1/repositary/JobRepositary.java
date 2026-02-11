package com.example.project1.repositary;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project1.module.Job;
@Repository
public interface JobRepositary extends JpaRepository<Job, Long> {
    List<Job> findByLocationContainingIgnoreCase(String location);
    List<Job> findByTitleContainingIgnoreCase(String title);

    List<Job> findByEmployerId(Long employerId);
    
}
