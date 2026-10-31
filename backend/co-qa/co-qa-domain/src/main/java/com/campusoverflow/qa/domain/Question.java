package com.campusoverflow.qa.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Question {

    private final Long id;
    private final Long authorUserId;
    private Title title;
    private MarkdownBody markdownBody;
    private Set<TagName> tags;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<Answer> answers;

    private Question(Long id, Long authorUserId, Title title, MarkdownBody markdownBody, Set<TagName> tags) {
        this.id = id;
        this.authorUserId = authorUserId;
        this.title = title;
        this.markdownBody = markdownBody;
        this.tags = tags;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.answers = new ArrayList<>();
    }

    /**
     * 聚合根工厂方法，领域不变式校验
     */
    public static Question create(Long id, Long authorUserId, Title title, MarkdownBody markdownBody, Set<TagName> tags) {
        if (authorUserId == null || authorUserId <= 0) {
            throw new IllegalArgumentException("提问用户ID非法");
        }
        if (title == null) {
            throw new IllegalArgumentException("问题标题不能为空");
        }
        if (markdownBody == null) {
            throw new IllegalArgumentException("问题内容不能为空");
        }
        return new Question(id, authorUserId, title, markdownBody, tags);
    }

    // --------领域业务行为，不提供public setter--------
    public void changeTitle(Title newTitle) {
        if (newTitle == null) {
            throw new IllegalArgumentException("标题不能为空");
        }
        this.title = newTitle;
        this.updatedAt = LocalDateTime.now();
    }

    public void editBody(MarkdownBody newBody) {
        if (newBody == null) {
            throw new IllegalArgumentException("问题内容不能为空");
        }
        this.markdownBody = newBody;
        this.updatedAt = LocalDateTime.now();
    }

    public void addTag(TagName tagName) {
        tags.add(tagName);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 通过聚合根创建Answer，外部不能直接new Answer
     */
    public Answer addAnswer(Long answerId, Long answerAuthorId, MarkdownBody answerContent) {
        Answer answer = Answer.create(answerId, answerAuthorId, answerContent);
        answers.add(answer);
        this.updatedAt = LocalDateTime.now();
        return answer;
    }

    /**
     * 委托聚合根给内部Answer增加投票
     */
    public void voteAnswer(Long answerId, Vote vote) {
        Answer target = answers.stream()
                .filter(a -> a.getId().equals(answerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("找不到该回答"));
        target.addVote(vote);
        this.updatedAt = LocalDateTime.now();
    }

    // --------getter--------
    public Long getId() { return id; }
    public Long getAuthorUserId() { return authorUserId; }
    public Title getTitle() { return title; }
    public MarkdownBody getMarkdownBody() { return markdownBody; }
    public Set<TagName> getTags() { return tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<Answer> getAnswers() { return List.copyOf(answers); }
}