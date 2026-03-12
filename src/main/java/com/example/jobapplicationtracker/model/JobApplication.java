package com.example.jobapplicationtracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "job_applications")
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "job_role", nullable = false)
    private String jobRole;

    @Column(name = "date_applied")
    private LocalDate dateApplied;

    @Column(name = "status")
    private String status; // APPLIED, INTERVIEW, OFFER, REJECTED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public JobApplication() {
    }

    public JobApplication(String companyName, String jobRole, LocalDate dateApplied, String status, String notes) {
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.dateApplied = dateApplied;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public LocalDate getDateApplied() {
        return dateApplied;
    }

    public void setDateApplied(LocalDate dateApplied) {
        this.dateApplied = dateApplied;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
