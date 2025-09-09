package com.example.demo.service;

import com.example.demo.model.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    // Fake database using a Map
    private Map<Long, Document> documents = new HashMap<>();
    public DocumentService() {
        // Initialize with sample data
        documents.put(1L, new Document(1L, "Admin Report", "Secret admin content", "admin"));
        documents.put(2L, new Document(2L, "User Guide", "How to use the app", "user"));
        documents.put(3L, new Document(3L, "Manager Notes", "Team performance", "manager"));
        documents.put(4L, new Document(4L, "Public Doc", "Everyone can see", "user"));
    }

    public Document findById(Long id) {
        return documents.get(id);
    }

    public List<Document> findAll() {
        return new ArrayList<>(documents.values());
    }

    public Document save(Document doc) {
        documents.put(doc.getId(), doc);
        return doc;
    }


}
