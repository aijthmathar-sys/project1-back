package com.example.project1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.example.project1.module.Job;
import com.example.project1.module.JobApplication;
import com.example.project1.service.JobService;
import com.example.project1.service.JobApplicationService;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    @Autowired
    private JobService jobService;
    @Autowired
    private JobApplicationService jobApplicationService;

    // Employer endpoints
    @PostMapping("/create")
    public Job createJob(@RequestBody Job job, Authentication authentication) {
        String userEmail = authentication.getName();
        return jobService.createJobByEmail(job, userEmail);
    }
 
    
    
    @DeleteMapping("/delete/{jobId}")
    public String deleteJob(@PathVariable Long jobId) {
        jobService.deleteJob(jobId);
        return "Job deleted successfully";
    }
    
    @GetMapping("/list/{employerId}")
    public List<Job> getEmployerJobs(@PathVariable Long employerId) {
        return jobService.getJobsByEmployer(employerId);
    }
    
    @GetMapping("/applicants/{jobId}")
    public List<JobApplication> viewApplicants(@PathVariable Long jobId) {
        return jobApplicationService.getApplicantsForJob(jobId);
    }
    
    // Job Seeker endpoints
    @GetMapping("/search")
    public List<Job> searchJobs(@RequestParam(required = false) String location, 
                                 @RequestParam(required = false) String title) {
        if (location != null && !location.isEmpty()) {
            return jobService.getJobsByLocation(location);
        }
        if (title != null && !title.isEmpty()) {
            return jobService.getJobsByTitle(title);
        }
        return jobService.getAllJobs();
    }
    
    @PostMapping("/apply/{jobId}")
    public JobApplication applyForJob(@PathVariable Long jobId, Authentication authentication) {
        String userEmail = authentication.getName();
        return jobApplicationService.applyJobByEmail(jobId, userEmail);
    }
    
    @GetMapping("/all")
    public Iterable<Job> getAllJobs() {
        return jobService.getAllJobs();
    }
}
