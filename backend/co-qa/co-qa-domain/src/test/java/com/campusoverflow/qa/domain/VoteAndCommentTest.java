package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoteAndCommentTest {

    // ---------- Vote 相关测试（成员2负责）----------
    @Test
    void shouldCreateUpVote() {
        Vote vote = Vote.up(1L);
        assertThat(vote.isUpVote()).isTrue();
        assertThat(vote.getVoterUserId()).isEqualTo(1L);
    }

    @Test
    void shouldCreateDownVote() {
        Vote vote = Vote.down(2L);
        assertThat(vote.isUpVote()).isFalse();
    }

    @Test
    void sameVoterReplaceVoteOnAnswer() {
        Long voterId = 3L;
        Vote up = Vote.up(voterId);
        Vote down = Vote.down(voterId);

        Title title = new Title("测试投票替换的问题标题长度足够满足要求");
        MarkdownBody content = MarkdownBody.ofQuestion("测试内容足够长满足校验规则要求");
        Question q = Question.create(1L, 1L, title, content, Set.of());
        Answer answer = q.addAnswer(1001L, 2L, MarkdownBody.ofAnswer("回答内容长度足够满足校验规则"));

        answer.addVote(up);
        answer.addVote(down);

        assertThat(answer.getVotes()).hasSize(1);
        assertThat(answer.getVotes().iterator().next().isUpVote()).isFalse();
    }

    @Test
    void shouldRejectInvalidVoterId() {
        assertThatThrownBy(() -> Vote.create(null, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Vote.create(-1L, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- Comment 相关测试（其他队友负责，占位）----------
    // TODO：Comment领域代码由对应负责人实现后补充测试
}