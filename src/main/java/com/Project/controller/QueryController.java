package com.Project.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Project.service.DocumentStore;
import com.Project.service.LLMService;

@RestController
@RequestMapping("/query")
public class QueryController {

    @Autowired
    private LLMService llmService;

    @Autowired
    private DocumentStore documentStore;

    @PostMapping
    public String ask(@RequestBody Map<String, String> body,
                      OAuth2AuthenticationToken token) {

        String user = token.getName();
        String question = body.get("question");

        String context = documentStore.get(user);

        if (context == null) {
            return "No documents processed yet.";
        }

        return llmService.askLLM(context, question);
    }
}