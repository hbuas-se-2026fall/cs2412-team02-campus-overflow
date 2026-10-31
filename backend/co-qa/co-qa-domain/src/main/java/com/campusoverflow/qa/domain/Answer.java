package com.campusoverflow.qa.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Answer {

    private final Long id;
    private final Long authorUserId;
    private MarkdownBody markdownBody;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean accepted;

    private final List<Vote> votes;

    private Answer(Long id, Long authorUserId, MarkdownBody markdownBody) {
        this.id = id;
        this.authorUserId = authorUserId;
        this.markdownBody = markdownBody;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.accepted = false;
        this.votes = new ArrayList<>();
    }

    public static Answer create(Long id, Long authorUserId, MarkdownBody markdownBody) {
        if (authorUserId == null || authorUserId <= 0) {
            throw new IllegalArgumentException("回答作者ID非法");
        }
        if (markdownBody == null) {
            throw new IllegalArgumentException("回答内容不能为空");
        }
        return new Answer(id, authorUserId, markdownBody);
    }

    // 业务方法
    public void editContent(MarkdownBody newBody) {
        if (newBody == null) {
            throw new IllegalArgumentException("回答内容不能为空");
        }
        this.markdownBody = newBody;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsAccepted() {
        this.accepted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 同一个用户只能保留一次投票
     */
    public void addVote(Vote vote) {
        votes.removeIf(v -> v.getVoterUserId().equals(vote.getVoterUserId()));
        votes.add(vote);
    }

    // getter
    public Long getId() { return id; }
    public Long getAuthorUserId() { return authorUserId; }
    public MarkdownBody getMarkdownBody() { return markdownBody; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public boolean isAccepted() { return accepted; }
    public List<Vote> getVotes() { return List.copyOf(votes); }
}