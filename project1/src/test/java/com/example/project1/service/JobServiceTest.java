package com.example.project1.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.project1.module.Job;
import com.example.project1.module.User;
import com.example.project1.repositary.JobRepositary;
import com.example.project1.repositary.UserRepositary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepositary jobRepository;

    @Mock
    private UserRepositary userRepository;

    @InjectMocks
    private JobService jobService;

    private User employer;
    private Job job;

    @BeforeEach
    void setUp() {
        // Setup Employer
        employer = new User();
        employer.setId(1L);
        employer.setName("Jane Employer");
        employer.setEmail("employer@example.com");
        employer.setPhoneNumber("+14155552672");
        employer.setRole("EMPLOYER");

        // Setup Job
        job = new Job();
        job.setId(1L);
        job.setTitle("Software Engineer");
        job.setCompanyname("Tech Corp");
        job.setLocation("New York");
        job.setDescription("Exciting opportunity for a talented engineer");
        job.setEmployer(employer);
    }

    @Test
    void testCreateJobSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(employer));
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        Job result = jobService.createJob(job, 1L);

        assertNotNull(result);
        assertEquals(job.getTitle(), result.getTitle());
        assertEquals(employer.getId(), result.getEmployer().getId());
        verify(userRepository, times(1)).findById(1L);
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void testCreateJobEmployerNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            jobService.createJob(job, 1L);
        });

        assertEquals("Employer not found", exception.getMessage());
    }

    @Test
    void testCreateJobByEmailSuccess() {
        when(userRepository.findByEmail(employer.getEmail())).thenReturn(Optional.of(employer));
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        Job result = jobService.createJobByEmail(job, employer.getEmail());

        assertNotNull(result);
        assertEquals(job.getTitle(), result.getTitle());
        assertEquals(employer.getId(), result.getEmployer().getId());
        verify(userRepository, times(1)).findByEmail(employer.getEmail());
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void testCreateJobByEmailEmployerNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            jobService.createJobByEmail(job, "nonexistent@example.com");
        });

        assertEquals("Employer not found", exception.getMessage());
    }

    @Test
    void testGetAllJobsSuccess() {
        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        jobs.add(new Job());

        when(jobRepository.findAll()).thenReturn(jobs);

        List<Job> result = jobService.getAllJobs();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    void testGetAllJobsEmpty() {
        when(jobRepository.findAll()).thenReturn(new ArrayList<>());

        List<Job> result = jobService.getAllJobs();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetJobsByLocationSuccess() {
        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        when(jobRepository.findByLocationContainingIgnoreCase("New York")).thenReturn(jobs);

        List<Job> result = jobService.getJobsByLocation("New York");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("New York", result.get(0).getLocation());
        verify(jobRepository, times(1)).findByLocationContainingIgnoreCase("New York");
    }

    @Test
    void testGetJobsByLocationNoResults() {
        when(jobRepository.findByLocationContainingIgnoreCase("London")).thenReturn(new ArrayList<>());

        List<Job> result = jobService.getJobsByLocation("London");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetJobsByEmployerSuccess() {
        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        when(jobRepository.findByEmployerId(1L)).thenReturn(jobs);

        List<Job> result = jobService.getJobsByEmployer(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(employer.getId(), result.get(0).getEmployer().getId());
        verify(jobRepository, times(1)).findByEmployerId(1L);
    }

    @Test
    void testGetJobsByEmployerNoResults() {
        when(jobRepository.findByEmployerId(99L)).thenReturn(new ArrayList<>());

        List<Job> result = jobService.getJobsByEmployer(99L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetJobsByTitleSuccess() {
        List<Job> jobs = new ArrayList<>();
        jobs.add(job);
        Job job2 = new Job();
        job2.setTitle("Senior Developer");
        jobs.add(job2);

        when(jobRepository.findByTitleContainingIgnoreCase("Software")).thenReturn(List.of(job));

        List<Job> result = jobService.getJobsByTitle("Software");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().contains("Software"));
        verify(jobRepository, times(1)).findByTitleContainingIgnoreCase("Software");
    }

    @Test
    void testGetJobsByTitleNoResults() {
        when(jobRepository.findByTitleContainingIgnoreCase("Manager")).thenReturn(new ArrayList<>());

        List<Job> result = jobService.getJobsByTitle("Manager");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testDeleteJob() {
        doNothing().when(jobRepository).deleteById(1L);

        jobService.deleteJob(1L);

        verify(jobRepository, times(1)).deleteById(1L);
    }
}
