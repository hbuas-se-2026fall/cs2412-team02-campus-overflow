package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AnswerTest {

    @Test
    void shouldCreateAnswerSuccess() {
        MarkdownBody body = MarkdownBody.ofAnswer("回答测试内容");
        Answer answer = Answer.create(100L, 200L, body);

        assertThat(answer).isNotNull();
        assertThat(answer.getQuestionId()).isEqualTo(100L);
        assertThat(answer.getAuthorId()).isEqualTo(200L);
        assertThat(answer.getBody()).isEqualTo(body);
    }

    @Test
    void shouldAddVoteToAnswer() {
        MarkdownBody body = MarkdownBody.ofAnswer("回答测试内容");
        Answer answer = Answer.create(100L, 200L, body);
        Vote vote = Vote.create(999L, 10L, true);
        answer.addVote(vote);

        assertThat(answer.getVotes()).hasSize(1);
        assertThat(answer.getVotes().get(0)).isSameAs(vote);
    }
}