package com.Project.service;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DocumentStore {

    // user → list of document texts
    private Map<String, List<String>> userDocs = new ConcurrentHashMap<>();

    // ✅ Save documents
    public void saveDocuments(String user, List<String> docs) {
        if (docs == null || docs.isEmpty()) {
            return;
        }
        userDocs.put(user, docs);
    }

    // ✅ Get raw documents
    public List<String> getDocuments(String user) {
        return userDocs.getOrDefault(user, new ArrayList<>());
    }

    // 🔥 IMPORTANT: Get combined context for LLM
    public String get(String user) {

        List<String> docs = userDocs.get(user);

        if (docs == null || docs.isEmpty()) {
            return null;
        }

        // Combine all docs into one string
        return String.join("\n\n", docs);
    }

    // ✅ Check if documents exist
    public boolean hasDocuments(String user) {
        List<String> docs = userDocs.get(user);
        return docs != null && !docs.isEmpty();
    }

    // ✅ Clear documents (optional)
    public void clear(String user) {
        userDocs.remove(user);
    }
}