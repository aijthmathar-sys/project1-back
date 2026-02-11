package com.example.project1.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.project1.module.Job;
import com.example.project1.module.JobApplication;
import com.example.project1.module.User;
import com.example.project1.repositary.JobApplicationRepositary;
import com.example.project1.repositary.JobRepositary;
import com.example.project1.repositary.UserRepositary;

@Service
public class JobApplicationService {
    @Autowired
    private JobApplicationRepositary jobApplicationRepositary;
    @Autowired
    private UserRepositary userRepositary;
    @Autowired
    private JobRepositary jobRepositary;
    @Autowired
    private SmsService smsService;
   
     //apply for a job
    public JobApplication applyJob(Long jobId, Long userId) {
        Job job=jobRepositary.findById(jobId).orElseThrow(()->new RuntimeException("Job not found"));
        User user=userRepositary.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        JobApplication jobApplication=new JobApplication();
        jobApplication.setJob(job);
        jobApplication.setUser(user);
        jobApplication.setStatus("Applied");
        JobApplication savedApplication = jobApplicationRepositary.save(jobApplication);
        
        // Send SMS to job seeker
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            smsService.sendApplicationConfirmationSms(user.getPhoneNumber(), job.getTitle(), job.getCompanyname());
        }
        
        // Send SMS to employer
        if (job.getEmployer() != null && job.getEmployer().getPhoneNumber() != null && !job.getEmployer().getPhoneNumber().isEmpty()) {
            smsService.sendApplicationToEmployerSms(job.getEmployer().getPhoneNumber(), user.getName(), job.getTitle());
        }
        
        return savedApplication;
    }
    public List<JobApplication> getApplicationsByUser(Long userId){
        return jobApplicationRepositary.findByUserId(userId);
    }

    public List<JobApplication> getApplicationsByJob(Long jobId){
        return jobApplicationRepositary.findByJobId(jobId);
    }
    
    public JobApplication applyJobByEmail(Long jobId, String userEmail) {
        User user = userRepositary.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Job job = jobRepositary.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        JobApplication jobApplication = new JobApplication();
        jobApplication.setJob(job);
        jobApplication.setUser(user);
        jobApplication.setStatus("Applied");
        JobApplication savedApplication = jobApplicationRepositary.save(jobApplication);
        
        // Send SMS to job seeker
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            smsService.sendApplicationConfirmationSms(user.getPhoneNumber(), job.getTitle(), job.getCompanyname());
        }
        
        // Send SMS to employer
        if (job.getEmployer() != null && job.getEmployer().getPhoneNumber() != null && !job.getEmployer().getPhoneNumber().isEmpty()) {
            smsService.sendApplicationToEmployerSms(job.getEmployer().getPhoneNumber(), user.getName(), job.getTitle());
        }
        
        return savedApplication;
    }
    
    public List<JobApplication> getApplicantsForJob(Long jobId) {
        jobRepositary.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        return jobApplicationRepositary.findByJobId(jobId);
    }
    
    public List<JobApplication> getApplicantsForJobByEmployer(Long jobId, Long employerId) {
        Job job = jobRepositary.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        if (!job.getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("You are not authorized to view applicants for this job");
        }
        
        return jobApplicationRepositary.findByJobId(jobId);

    }

}



    
    
