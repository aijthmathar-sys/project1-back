package com.example.project1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.project1.module.JobApplication;
import com.example.project1.service.JobApplicationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/applications")
public class JobApplicationController {

    @Autowired
    private JobApplicationService jobApplicationService;

    @PostMapping("/apply")
   public JobApplication applyjob(@RequestParam Long jobId,@RequestParam Long userId){
        return jobApplicationService.applyJob(jobId,userId);
   }
    @GetMapping("/user/{userId}")
    public List<JobApplication> getApplicationsByUser(@PathVariable Long userId) {
        return jobApplicationService.getApplicationsByUser(userId);
    }
    
    
}
