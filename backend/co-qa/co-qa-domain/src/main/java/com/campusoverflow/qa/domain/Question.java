package com.campusoverflow.qa.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Question {
    private final Long id;
    private final Long authorId;
    private final Title title;
    private final MarkdownBody content;
    private final Set<TagName> tags;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant deletedAt;
    private final List<Answer> answers;

    private Question(Long id, Long authorId, Title title, MarkdownBody content, Set<TagName> tags, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.answers = new ArrayList<>();
    }

    public static Question create(Long authorId, Title title, MarkdownBody content, Set<TagName> tags) {
        if (authorId == null) {
            throw new IllegalArgumentException("作者id不能为空");
        }
        if (title == null) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (content == null) {
            throw new IllegalArgumentException("内容不能为空");
        }
        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("标签不能为空");
        }
        Instant now = Instant.now();
        return new Question(null, authorId, title, content, tags, now, null, null);
    }

    public static Question reconstitute(Long id, Long authorId, Title title, MarkdownBody content, Set<TagName> tags, Instant createdAt, Instant updatedAt, Instant deletedAt, List<Answer> answers) {
        Question q = new Question(id, authorId, title, content, tags, createdAt, updatedAt, deletedAt);
        q.answers.addAll(answers);
        return q;
    }

    public void addAnswer(Answer answer) {
        this.answers.add(answer);
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    // getter
    public Long getId() {
        return id;
    }
    public Long getAuthorId() {
        return authorId;
    }
    public Title getTitle() {
        return title;
    }
    public MarkdownBody getContent() {
        return content;
    }
    public Set<TagName> getTags() {
        return tags;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public Instant getDeletedAt() {
        return deletedAt;
    }
}