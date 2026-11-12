package com.campusoverflow.qa.domain;

public class Vote {
    private final Long id;
    private final Long voterId;
    private final Long targetId;
    private final boolean upVote;

    private Vote(Long id, Long voterId, Long targetId, boolean upVote) {
        this.id = id;
        this.voterId = voterId;
        this.targetId = targetId;
        this.upVote = upVote;
    }

    public static Vote create(Long voterId, Long targetId, boolean upVote) {
        if (voterId == null) {
            throw new IllegalArgumentException("投票人id不能为空");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("目标id不能为空");
        }
        return new Vote(null, voterId, targetId, upVote);
    }

    public static Vote reconstitute(Long id, Long voterId, Long targetId, boolean upVote) {
        return new Vote(id, voterId, targetId, upVote);
    }

    public boolean isUpVote() {
        return upVote;
    }

    public Long getId() {
        return id;
    }
    public Long getVoterId() {
        return voterId;
    }
    public Long getTargetId() {
        return targetId;
    }
}