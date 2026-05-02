package com.Project.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.Project.service.DriveService;
import com.Project.service.DocumentStore;

@RestController
@RequestMapping("/drive")
public class DriveController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private DriveService driveService;

    @Autowired
    private DocumentStore documentStore; // ✅ FIX: inject it

    // 🔹 Store selected folder per user
    private Map<String, String> userFolderMap = new ConcurrentHashMap<>();

    // 🔹 Helper to safely get access token
    private String getAccessToken(OAuth2AuthenticationToken token) {
        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        token.getAuthorizedClientRegistrationId(),
                        token.getName()
                );

        if (client == null || client.getAccessToken() == null) {
            throw new RuntimeException("User not authenticated. Please login again.");
        }

        return client.getAccessToken().getTokenValue();
    }

    // ✅ 1. Get folders
    @GetMapping("/folders")
    public ResponseEntity<?> getFolders(OAuth2AuthenticationToken token) {

        String accessToken = getAccessToken(token);

        String folders = driveService.listFolders(accessToken);

        return ResponseEntity.ok(folders);
    }

    // ✅ 2. Select folder
    @PostMapping("/select-folder")
    public ResponseEntity<?> selectFolder(@RequestBody Map<String, String> body,
                                          OAuth2AuthenticationToken token) {

        String folderId = body.get("folderId");

        if (folderId == null || folderId.isBlank()) {
            return ResponseEntity.badRequest().body("Folder ID is required");
        }

        userFolderMap.put(token.getName(), folderId);

        // 🔥 Clear old docs when new folder selected
        documentStore.clear(token.getName());

        return ResponseEntity.ok("Folder selected successfully");
    }

    // ✅ 3. Get selected folder
    @GetMapping("/selected-folder")
    public ResponseEntity<?> getSelectedFolder(OAuth2AuthenticationToken token) {

        String folderId = userFolderMap.get(token.getName());

        if (folderId == null) {
            return ResponseEntity.badRequest().body("No folder selected");
        }

        return ResponseEntity.ok(folderId);
    }

    // 🔥 4. Process files (MOST IMPORTANT)
    @GetMapping("/process-files")
    public ResponseEntity<?> processFiles(OAuth2AuthenticationToken token) {

        String user = token.getName();

        String folderId = userFolderMap.get(user);

        if (folderId == null) {
            return ResponseEntity.badRequest().body("No folder selected");
        }

        try {
            String accessToken = getAccessToken(token);

            // 🔥 Fetch + extract documents
            List<String> docs = driveService.processFiles(accessToken, folderId);

            if (docs.isEmpty()) {
                return ResponseEntity.ok("No supported documents found");
            }

            // 🔥 Store documents (NOT context string)
            documentStore.saveDocuments(user, docs);

            return ResponseEntity.ok("Documents processed successfully");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error processing files: " + e.getMessage());
        }
    }
}