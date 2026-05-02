package com.Project.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DriveService {

    private final WebClient webClient;

    public DriveService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://www.googleapis.com/drive/v3")
                .build();
    }

    // ✅ Fetch only folders
    public String listFolders(String accessToken) {

        String query = "mimeType='application/vnd.google-apps.folder'";

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/files")
                        .queryParam("q", query)
                        .queryParam("fields", "files(id,name)")
                        .build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // ✅ Process files inside selected folder
    public List<String> processFiles(String accessToken, String folderId) throws Exception {

        String query = "'" + folderId + "' in parents";

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/files")
                        .queryParam("q", query)
                        .queryParam("fields", "files(id,name,mimeType)")
                        .build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        List<String> documents = new ArrayList<>();

        if (root.get("files") == null || !root.get("files").isArray()) {
            System.out.println("No files found in folder");
            return documents;
        }

        Tika tika = new Tika();

        // 🔹 Iterate files
        for (JsonNode file : root.get("files")) {

            String fileId = file.get("id").asText();
            String fileName = file.get("name").asText();
            String mimeType = file.get("mimeType").asText();

            System.out.println("Processing file: " + fileName + " | " + mimeType);

            byte[] data = null;

            try {

                // 🔥 Google Docs → export
                if ("application/vnd.google-apps.document".equals(mimeType)) {

                    data = webClient.get()
                            .uri("/files/" + fileId + "/export?mimeType=text/plain")
                            .headers(headers -> headers.setBearerAuth(accessToken))
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .block();

                }

                // 🔥 Other supported files
                else if (mimeType.contains("pdf") ||
                         mimeType.contains("text") ||
                         mimeType.contains("word") ||
                         mimeType.contains("markdown")) {

                    data = webClient.get()
                            .uri("/files/" + fileId + "?alt=media")
                            .headers(headers -> headers.setBearerAuth(accessToken))
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .block();

                } else {
                    continue;
                }

                if (data == null || data.length == 0) {
                    continue;
                }

                // 🔹 Extract text
                String text = tika.parseToString(new ByteArrayInputStream(data));

                if (text != null && !text.isBlank()) {

                    // 🔥 ADD SOURCE (IMPORTANT CHANGE)
                    String documentWithSource =
                            "SOURCE: " + fileName + "\n" + text.trim();

                    documents.add(documentWithSource);

                    System.out.println("Extracted text length: " + text.length());
                }

            } catch (Exception e) {
                System.out.println("Failed file: " + fileName + " -> " + e.getMessage());
            }
        }

        System.out.println("Total documents extracted: " + documents.size());

        return documents;
    }
}