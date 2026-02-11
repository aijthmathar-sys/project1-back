package com.example.project1.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.project1.module.Job;
import com.example.project1.module.JobApplication;
import com.example.project1.module.User;
import com.example.project1.repositary.JobApplicationRepositary;
import com.example.project1.repositary.JobRepositary;
import com.example.project1.repositary.UserRepositary;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepositary jobApplicationRepositary;

    @Mock
    private UserRepositary userRepositary;

    @Mock
    private JobRepositary jobRepositary;

    @Mock
    private SmsService smsService;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private User jobSeeker;
    private User employer;
    private Job job;
    private JobApplication jobApplication;

    @BeforeEach
    void setUp() {

        jobSeeker = new User();
        jobSeeker.setId(1L);
        jobSeeker.setName("John");
        jobSeeker.setEmail("john@example.com");
        jobSeeker.setPhoneNumber("+919876543210");

        employer = new User();
        employer.setId(2L);
        employer.setName("Employer");
        employer.setPhoneNumber("+919876543211");

        job = new Job();
        job.setId(1L);
        job.setTitle("Software Engineer");
        job.setCompanyname("Tech Corp");
        job.setEmployer(employer);

        jobApplication = new JobApplication();
        jobApplication.setId(1L);
        jobApplication.setJob(job);
        jobApplication.setUser(jobSeeker);
        jobApplication.setStatus("Applied");
    }

    // ---------------- APPLY JOB ----------------

    @Test
    void testApplyJobSuccess() {

        when(jobRepositary.findById(1L)).thenReturn(Optional.of(job));
        when(userRepositary.findById(1L)).thenReturn(Optional.of(jobSeeker));
        when(jobApplicationRepositary.save(any(JobApplication.class)))
                .thenReturn(jobApplication);

        JobApplication result = jobApplicationService.applyJob(1L, 1L);

        assertNotNull(result);
        assertEquals("Applied", result.getStatus());

        verify(jobApplicationRepositary, times(1))
                .save(any(JobApplication.class));

        verify(smsService, times(1))
                .sendApplicationConfirmationSms(
                        eq(jobSeeker.getPhoneNumber()),
                        eq(job.getTitle()),
                        eq(job.getCompanyname())
                );

        verify(smsService, times(1))
                .sendApplicationToEmployerSms(
                        eq(employer.getPhoneNumber()),
                        eq(jobSeeker.getName()),
                        eq(job.getTitle())
                );
    }

    @Test
    void testApplyJobJobNotFound() {

        when(jobRepositary.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                jobApplicationService.applyJob(1L, 1L));

        assertEquals("Job not found", exception.getMessage());
    }

    @Test
    void testApplyJobUserNotFound() {

        when(jobRepositary.findById(1L)).thenReturn(Optional.of(job));
        when(userRepositary.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                jobApplicationService.applyJob(1L, 1L));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testApplyJobWithoutUserPhone() {

        jobSeeker.setPhoneNumber(null);

        when(jobRepositary.findById(1L)).thenReturn(Optional.of(job));
        when(userRepositary.findById(1L)).thenReturn(Optional.of(jobSeeker));
        when(jobApplicationRepositary.save(any(JobApplication.class)))
                .thenReturn(jobApplication);

        jobApplicationService.applyJob(1L, 1L);

        verify(smsService, never())
                .sendApplicationConfirmationSms(anyString(), anyString(), anyString());

        verify(smsService, times(1))
                .sendApplicationToEmployerSms(anyString(), anyString(), anyString());
    }

    // ---------------- APPLY BY EMAIL ----------------

    @Test
    void testApplyJobByEmailSuccess() {

        when(userRepositary.findByEmail(jobSeeker.getEmail()))
                .thenReturn(Optional.of(jobSeeker));

        when(jobRepositary.findById(1L)).thenReturn(Optional.of(job));

        when(jobApplicationRepositary.save(any(JobApplication.class)))
                .thenReturn(jobApplication);

        JobApplication result = jobApplicationService.applyJobByEmail(1L, jobSeeker.getEmail());

        assertNotNull(result);
        assertEquals(jobSeeker.getId(), result.getUser().getId());
    }

    @Test
    void testApplyJobByEmailUserNotFound() {

        when(userRepositary.findByEmail("wrong@email.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                jobApplicationService.applyJobByEmail(1L, "wrong@email.com"));

        assertEquals("User not found", exception.getMessage());
    }

    // ---------------- GET APPLICATIONS ----------------

    @Test
    void testGetApplicationsByUser() {

        List<JobApplication> list = new ArrayList<>();
        list.add(jobApplication);

        when(jobApplicationRepositary.findByUserId(1L)).thenReturn(list);

        List<JobApplication> result =
                jobApplicationService.getApplicationsByUser(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testGetApplicationsByJob() {

        List<JobApplication> list = new ArrayList<>();
        list.add(jobApplication);

        when(jobApplicationRepositary.findByJobId(1L)).thenReturn(list);

        List<JobApplication> result =
                jobApplicationService.getApplicationsByJob(1L);

        assertEquals(1, result.size());
    }

    // ---------------- GET APPLICANTS ----------------

    @Test
    void testGetApplicantsForJobSuccess() {

        when(jobRepositary.findById(1L)).thenReturn(Optional.of(job));
        when(jobApplicationRepositary.findByJobId(1L))
                .thenReturn(List.of(jobApplication));

        List<JobApplication> result =
                jobApplicationService.getApplicantsForJob(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testGetApplicantsForJobNotFound() {

        when(jobRepositary.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                jobApplicationService.getApplicantsForJob(1L));

        assertEquals("Job not found", exception.getMessage());
    }

    @Test
    void testGetApplicantsForJobByEmployerSuccess() {

        when(jobRepositary.findById(1L)).thenReturn(Optional.of(job));
        when(jobApplicationRepositary.findByJobId(1L))
                .thenReturn(List.of(jobApplication));

        List<JobApplication> result =
                jobApplicationService.getApplicantsForJobByEmployer(1L, employer.getId());

        assertEquals(1, result.size());
    }

    @Test
    void testGetApplicantsForJobByEmployerUnauthorized() {

        User otherEmployer = new User();
        otherEmployer.setId(99L);
        job.setEmployer(otherEmployer);

        when(jobRepositary.findById(1L)).thenReturn(Optional.of(job));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                jobApplicationService.getApplicantsForJobByEmployer(1L, employer.getId()));

        assertEquals("You are not authorized to view applicants for this job",
                exception.getMessage());
    }
}
