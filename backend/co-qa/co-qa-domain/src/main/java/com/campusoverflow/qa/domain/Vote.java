package com.campusoverflow.qa.domain;

import java.util.Objects;

public final class Vote {
    private final Long voterUserId;
    private final boolean isUpVote;

    private Vote(Long voterUserId, boolean isUpVote) {
        this.voterUserId = voterUserId;
        this.isUpVote = isUpVote;
    }

    public static Vote create(Long voterUserId, boolean isUpVote) {
        if (voterUserId == null || voterUserId <= 0) {
            throw new IllegalArgumentException("投票用户ID非法");
        }
        return new Vote(voterUserId, isUpVote);
    }

    public static Vote up(Long voterUserId) {
        return create(voterUserId, true);
    }

    public static Vote down(Long voterUserId) {
        return create(voterUserId, false);
    }

    public Long getVoterUserId() {
        return voterUserId;
    }

    public boolean isUpVote() {
        return isUpVote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vote vote = (Vote) o;
        return isUpVote == vote.isUpVote && Objects.equals(voterUserId, vote.voterUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(voterUserId, isUpVote);
    }
}