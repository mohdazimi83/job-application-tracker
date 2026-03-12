package com.example.jobapplicationtracker.controller;

import com.example.jobapplicationtracker.model.JobApplication;
import com.example.jobapplicationtracker.service.JobApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    @Autowired
    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createApplication(@RequestBody JobApplication application) {
        // Basic validation
        if (application.getCompanyName() == null || application.getCompanyName().trim().isEmpty() ||
            application.getJobRole() == null || application.getJobRole().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Company name and job role cannot be empty");
        }
        
        JobApplication created = service.createApplication(application);
        return new ResponseEntity<>(created, HttpStatus.CREATED); // 201 Created
    }

    @GetMapping
    public ResponseEntity<List<JobApplication>> getAllApplications(@RequestParam(required = false) String status) {
        List<JobApplication> applications;
        if (status != null && !status.trim().isEmpty()) {
            applications = service.getApplicationsByStatus(status);
        } else {
            applications = service.getAllApplications();
        }
        return ResponseEntity.ok(applications); // 200 OK
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplication> updateApplication(@PathVariable Long id, @RequestBody JobApplication applicationDetails) {
        JobApplication updated = service.updateApplication(id, applicationDetails);
        return ResponseEntity.ok(updated); // 200 OK, will throw 404 if not found
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        service.deleteApplication(id);
        return ResponseEntity.noContent().build(); // 204 No Content, will throw 404 if not found
    }
}
