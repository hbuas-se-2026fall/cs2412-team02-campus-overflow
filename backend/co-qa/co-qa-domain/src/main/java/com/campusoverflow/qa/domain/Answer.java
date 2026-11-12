package com.campusoverflow.qa.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Answer {
    private final Long id;
    private final Long questionId;
    private final Long authorId;
    private final MarkdownBody body;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant deletedAt;
    private final List<Vote> votes;

    private Answer(Long id, Long questionId, Long authorId, MarkdownBody body, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.id = id;
        this.questionId = questionId;
        this.authorId = authorId;
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.votes = new ArrayList<>();
    }

    public static Answer create(Long questionId, Long authorId, MarkdownBody body) {
        if (questionId == null) {
            throw new IllegalArgumentException("问题id不能为空");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("回答作者id不能为空");
        }
        if (body == null) {
            throw new IllegalArgumentException("回答正文不能为空");
        }
        Instant now = Instant.now();
        return new Answer(null, questionId, authorId, body, now, null, null);
    }

    public static Answer reconstitute(Long id, Long questionId, Long authorId, MarkdownBody body, Instant createdAt, Instant updatedAt, Instant deletedAt, List<Vote> votes) {
        Answer ans = new Answer(id, questionId, authorId, body, createdAt, updatedAt, deletedAt);
        ans.votes.addAll(votes);
        return ans;
    }

    public void addVote(Vote vote) {
        this.votes.add(vote);
    }

    public List<Vote> getVotes() {
        return votes;
    }

    // getter
    public Long getId() {
        return id;
    }
    public Long getQuestionId() {
        return questionId;
    }
    public Long getAuthorId() {
        return authorId;
    }
    public MarkdownBody getBody() {
        return body;
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