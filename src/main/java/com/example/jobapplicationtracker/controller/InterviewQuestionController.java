package com.example.jobapplicationtracker.controller;

import com.example.jobapplicationtracker.model.InterviewQuestion;
import com.example.jobapplicationtracker.service.InterviewQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{id}")
public class InterviewQuestionController {

    private static final List<InterviewQuestion> FALLBACK_RESPONSE;

    static {
        InterviewQuestion q1 = new InterviewQuestion();
        q1.setQuestionText("Tell me about yourself and your background in this field.");
        q1.setGeneratedBy("Fallback");

        InterviewQuestion q2 = new InterviewQuestion();
        q2.setQuestionText("How do you approach problem-solving when you face a technical challenge?");
        q2.setGeneratedBy("Fallback");

        InterviewQuestion q3 = new InterviewQuestion();
        q3.setQuestionText("Where do you see yourself growing in this role over the next year?");
        q3.setGeneratedBy("Fallback");

        FALLBACK_RESPONSE = List.of(q1, q2, q3);
    }

    private final InterviewQuestionService service;

    @Autowired
    public InterviewQuestionController(InterviewQuestionService service) {
        this.service = service;
    }

    @PostMapping("/prepare")
    public ResponseEntity<List<InterviewQuestion>> prepareQuestions(@PathVariable("id") Long applicationId) {
        try {
            List<InterviewQuestion> preparedQuestions = service.prepareQuestions(applicationId);
            return ResponseEntity.ok(preparedQuestions);
        } catch (Exception e) {
            // Never return 500 — always return 200 with fallback questions
            System.err.println("prepareQuestions caught unexpected exception, returning fallback: " + e.getMessage());
            return ResponseEntity.ok(FALLBACK_RESPONSE);
        }
    }

    @GetMapping("/questions")
    public ResponseEntity<List<InterviewQuestion>> getQuestions(@PathVariable("id") Long applicationId) {
        List<InterviewQuestion> questions = service.getQuestionsForApplication(applicationId);
        return ResponseEntity.ok(questions);
    }
}
