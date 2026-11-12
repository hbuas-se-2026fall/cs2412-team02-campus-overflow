package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class VoteAndCommentTest {

    @Test
    void shouldCreateUpVote() {
        Vote vote = Vote.create(1001L, 501L, true);
        assertThat(vote.isUpVote()).isTrue();
        assertThat(vote.getVoterId()).isEqualTo(1001L);
        assertThat(vote.getTargetId()).isEqualTo(501L);
    }

    @Test
    void shouldCreateDownVote() {
        Vote vote = Vote.create(1002L, 501L, false);
        assertThat(vote.isUpVote()).isFalse();
    }
}