package com.example.jobapplicationtracker.service;

import com.example.jobapplicationtracker.exception.ResourceNotFoundException;
import com.example.jobapplicationtracker.model.JobApplication;
import com.example.jobapplicationtracker.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;

    @Autowired
    public JobApplicationService(JobApplicationRepository repository) {
        this.repository = repository;
    }

    public JobApplication createApplication(JobApplication application) {
        if (application.getStatus() == null || application.getStatus().trim().isEmpty()) {
            application.setStatus("APPLIED"); // Default status if none provided
        }
        return repository.save(application);
    }

    public List<JobApplication> getAllApplications() {
        return repository.findAll();
    }

    public List<JobApplication> getApplicationsByStatus(String status) {
        return repository.findByStatus(status);
    }

    public JobApplication updateApplication(Long id, JobApplication applicationDetails) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job Application not found with id: " + id));

        application.setStatus(applicationDetails.getStatus());
        application.setNotes(applicationDetails.getNotes());

        return repository.save(application);
    }

    public void deleteApplication(Long id) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job Application not found with id: " + id));
        repository.delete(application);
    }
}
