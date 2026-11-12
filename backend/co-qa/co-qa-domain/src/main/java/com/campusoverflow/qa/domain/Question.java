package com.campusoverflow.qa.domain;

public class Question {
    private Long id;
    private Long authorId;
    private String title;
    private String content;

    private Question(Long authorId, String title, String content) {
        this.authorId = authorId;
        this.title = title;
        this.content = content;
    }

    public static Question create(Long authorId, String title, String content) {
        return new Question(authorId, title, content);
    }

    public Long id() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorId() {
        return authorId;
    }
    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
}