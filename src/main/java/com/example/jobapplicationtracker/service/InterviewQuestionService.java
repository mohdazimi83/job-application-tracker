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

    private static final List<String> FALLBACK_QUESTION_TEXTS = List.of(
        "Tell me about yourself and your background in this field.",
        "How do you approach problem-solving when you face a technical challenge?",
        "Where do you see yourself growing in this role over the next year?"
    );

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

        List<String> generatedTextQuestions;
        String generatedBy;

        try {
            generatedTextQuestions = geminiService.generateInterviewQuestions(
                    application.getCompanyName(),
                    application.getJobRole()
            );
            generatedBy = "Gemini AI";
        } catch (Exception e) {
            System.err.println("GeminiService threw an exception, using fallback questions: " + e.getMessage());
            generatedTextQuestions = FALLBACK_QUESTION_TEXTS;
            generatedBy = "Fallback";
        }

        List<InterviewQuestion> savedQuestions = new ArrayList<>();

        if (generatedTextQuestions != null) {
            for (String text : generatedTextQuestions) {
                if (text != null && !text.isBlank()) {
                    InterviewQuestion question = new InterviewQuestion();
                    question.setApplicationId(applicationId);
                    question.setQuestionText(text);
                    question.setGeneratedBy(generatedBy);
                    // createdAt is handled by @PrePersist in the Entity

                    savedQuestions.add(questionRepository.save(question));
                }
            }
        }

        // Safety net: if nothing was saved at all, save fallback questions
        if (savedQuestions.isEmpty()) {
            for (String text : FALLBACK_QUESTION_TEXTS) {
                InterviewQuestion question = new InterviewQuestion();
                question.setApplicationId(applicationId);
                question.setQuestionText(text);
                question.setGeneratedBy("Fallback");
                savedQuestions.add(questionRepository.save(question));
            }
        }

        return savedQuestions;
    }
}
