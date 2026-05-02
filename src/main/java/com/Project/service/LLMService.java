package com.Project.service;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LLMService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();

    public String askLLM(String context, String question) {

        if (context == null || context.isBlank()) {
            return "No documents found. Please process the selected folder.";
        }

        String prompt = """
        		You are a strict document-based AI assistant.

        		RULES:
        		- Answer ONLY using the provided context.
        		- ALWAYS include the source file name.
        		- The final output MUST follow this format:

        		<answer>

        		SOURCE: <file_name>

        		- Do NOT skip SOURCE
        		- Do NOT use outside knowledge
        		- If answer not found, return:
        		  Not found in the selected folder documents.

        		CONTEXT:
        		""" + context + """

        		QUESTION:
        		""" + question;

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4o-mini");
        body.put("temperature", 0);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", prompt)
        );

        body.put("messages", messages);

        String response = webClient.post()
                .uri("/chat/completions")
                .headers(headers -> {
                    headers.setBearerAuth(apiKey);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> {
                    List choices = (List) res.get("choices");
                    Map choice = (Map) choices.get(0);
                    Map message = (Map) choice.get("message");
                    return message.get("content").toString();
                })
                .block();

        // 🔴 Final safety check
        if (response == null || response.toLowerCase().contains("not sure")) {
            return "Not found in the selected folder documents.";
        }

        return response;
    }
}