package com.example.project1.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.project1.module.Job;
import com.example.project1.module.User;
import com.example.project1.repositary.JobRepositary;
import com.example.project1.repositary.UserRepositary;

@Service
public class JobService {
    @Autowired
    private JobRepositary jobRepository;
    @Autowired
    private UserRepositary userRepository;

    public Job createJob(Job job, Long employerId) {
        User employer = userRepository.findById(employerId)
                .orElseThrow(() -> new RuntimeException("Employer not found"));
        job.setEmployer(employer);
        return jobRepository.save(job);
    }
    
    public Job createJobByEmail(Job job, String employerEmail) {
        User employer = userRepository.findByEmail(employerEmail)
                .orElseThrow(() -> new RuntimeException("Employer not found"));
        job.setEmployer(employer);
        return jobRepository.save(job);
    }
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
    public List<Job> getJobsByLocation(String location) {
        return jobRepository.findByLocationContainingIgnoreCase(location);
    }
    public List<Job> getJobsByEmployer(Long employerId) {
        return jobRepository.findByEmployerId(employerId);
    }
    
    public List<Job> getJobsByTitle(String title) {
        // This assumes you have a method in repository
        return jobRepository.findByTitleContainingIgnoreCase(title);
    }
    
    public void deleteJob(Long jobId) {
        jobRepository.deleteById(jobId);
    }
  
}
