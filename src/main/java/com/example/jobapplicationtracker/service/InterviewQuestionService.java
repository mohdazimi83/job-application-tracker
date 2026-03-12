package com.example.jobapplicationtracker.service;

import com.example.jobapplicationtracker.exception.ResourceNotFoundException;
import com.example.jobapplicationtracker.model.InterviewQuestion;
import com.example.jobapplicationtracker.model.JobApplication;
import com.example.jobapplicationtracker.repository.InterviewQuestionRepository;
import com.example.jobapplicationtracker.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InterviewQuestionService {

    private final InterviewQuestionRepository questionRepository;
    private final JobApplicationRepository applicationRepository;
    private final GeminiService geminiService;

    @Autowired
    public InterviewQuestionService(InterviewQuestionRepository questionRepository, 
                                    JobApplicationRepository applicationRepository,
                                    GeminiService geminiService) {
        this.questionRepository = questionRepository;
        this.applicationRepository = applicationRepository;
        this.geminiService = geminiService;
    }

    public List<InterviewQuestion> getQuestionsForApplication(Long applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new ResourceNotFoundException("Job Application not found with id: " + applicationId);
        }
        return questionRepository.findByApplicationId(applicationId);
    }

    public List<InterviewQuestion> prepareQuestions(Long applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job Application not found with id: " + applicationId));

        List<String> generatedTextQuestions = geminiService.generateInterviewQuestions(
                application.getCompanyName(), 
                application.getJobRole()
        );

        List<InterviewQuestion> savedQuestions = new ArrayList<>();

        for (String text : generatedTextQuestions) {
            InterviewQuestion question = new InterviewQuestion();
            question.setApplicationId(applicationId);
            question.setQuestionText(text);
            question.setGeneratedBy("Gemini AI");
            // createdAt is handled by @PrePersist in the Entity

            savedQuestions.add(questionRepository.save(question));
        }

        return savedQuestions;
    }
}
