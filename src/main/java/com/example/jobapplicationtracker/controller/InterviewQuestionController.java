package com.example.jobapplicationtracker.controller;

import com.example.jobapplicationtracker.model.InterviewQuestion;
import com.example.jobapplicationtracker.service.InterviewQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{id}")
public class InterviewQuestionController {

    private final InterviewQuestionService service;

    @Autowired
    public InterviewQuestionController(InterviewQuestionService service) {
        this.service = service;
    }

    @PostMapping("/prepare")
    public ResponseEntity<List<InterviewQuestion>> prepareQuestions(@PathVariable("id") Long applicationId) {
        List<InterviewQuestion> preparedQuestions = service.prepareQuestions(applicationId);
        return new ResponseEntity<>(preparedQuestions, HttpStatus.CREATED);
    }

    @GetMapping("/questions")
    public ResponseEntity<List<InterviewQuestion>> getQuestions(@PathVariable("id") Long applicationId) {
        List<InterviewQuestion> questions = service.getQuestionsForApplication(applicationId);
        return ResponseEntity.ok(questions);
    }
}
