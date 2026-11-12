package com.campusoverflow.qa.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "qa_vote")
public class VoteJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long voterId;
    private String targetType;
    private Long targetId;
    private boolean upVote;

    public VoteJpaEntity(Long voterId, String targetType, Long targetId, boolean upVote) {
        this.voterId = voterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.upVote = upVote;
    }

    public VoteJpaEntity() {}

    // getter
    public Long getId() {
        return id;
    }
    public Long getVoterId() {
        return voterId;
    }
    public String getTargetType() {
        return targetType;
    }
    public Long getTargetId() {
        return targetId;
    }
    public boolean isUpVote() {
        return upVote;
    }

    // setter
    public void setUpVote(boolean upVote) {
        this.upVote = upVote;
    }
}