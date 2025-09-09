package com.example.demo.controller;

import com.example.demo.model.Document;
import com.example.demo.service.DocumentService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // 1. @Secured - Simple role check (OLD STYLE)
    // Only ADMIN or MANAGER can access
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    @GetMapping("/management-only")
    public String managementOnly() {
        return "This is only for ADMIN or MANAGER roles";
    }

    // 2. @PreAuthorize with parameter check
    // Users can only get their OWN documents (or ADMIN can get any)
    @PreAuthorize("#owner == authentication.name")
    @GetMapping("/by-owner/{owner}")
    public List<Document> getDocuementByOwner(@PathVariable String owner) {
        return documentService.findAll().stream().filter(doc -> doc.getOwner().equals(owner)).toList();
    }

    // 3. @PostAuthorize - Check AFTER fetching
    // Fetch document first, then check if user owns it
    @PostAuthorize("returnObject.owner == authentication.name or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Document getDocument(@PathVariable Long id) {
        Document doc = documentService.findById(id);
        // spring checks the ownership after fetching the doc
        return doc;
    }

    // 4. @PostFilter - Filter collection automatically
    // Returns ALL documents but filters to show only user's docs (or all for ADMIN)

    @PostFilter("filterObject.owner == authentication.name or hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Document> getAllDocuments() {
        return documentService.findAll();
        // Spring automatically removes documents user shouldn't see
    }

    // 5. Complex SpEL expression
    // Create document only if: USER role AND it's a weekday AND owner matches username
    @PreAuthorize("hasRole('USER') and " +
            "T(java.time.LocalDate).now().dayOfWeek.value <= 5 and "+
            "authentication.name == #document.owner")
    @PostMapping
    public void createDocument(Document document) {
        documentService.save(document);
    }

    // 6. Simple public endpoint for comparison
    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint has no security - anyone can access";
    }

    // Using custom security service
    @PreAuthorize("@customSecurity.isWorkingHours()")
    @GetMapping("/business-hours-only")
    public String businessHoursOnly() {
        return "This only works 9 AM to 5 PM";
    }

    @PreAuthorize("@customSecurity.isPremiumUser(authentication.name)")
    @GetMapping("/premium")
    public String premiumContent() {
        return "Premium content only!";
    }
}
