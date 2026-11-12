package com.campusoverflow.qa.domain;

public class Answer {
    private Long id;
    private Long questionId;
    private Long authorId;
    private String content;

    private Answer(Long questionId, Long authorId, String content) {
        this.questionId = questionId;
        this.authorId = authorId;
        this.content = content;
    }

    public static Answer create(Long questionId, Long authorId, String content) {
        return new Answer(questionId, authorId, content);
    }

    public Long id() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }
    public Long getAuthorId() {
        return authorId;
    }
    public String getContent() {
        return content;
    }
}