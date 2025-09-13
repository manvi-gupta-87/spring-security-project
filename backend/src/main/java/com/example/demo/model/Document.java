package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


public class Document {
    private Long id;
    private String title;
    private String content;
    private String owner;

    public Document() {}

    public Document(Long id, String title, String content, String owner) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
}


