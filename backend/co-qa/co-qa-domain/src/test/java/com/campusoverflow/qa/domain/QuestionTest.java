package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class QuestionTest {

    @Test
    void shouldCreateQuestionSuccess() {
        Title title = new Title("DDD怎么写聚合？");
        MarkdownBody body = new MarkdownBody("请教充血模型写法");
        TagName tag1 = new TagName("DDD");
        TagName tag2 = new TagName("SpringBoot");

        Question q = Question.create(1L, 1001L, title, body, Set.of(tag1, tag2));
        assertEquals(1L, q.getId());
        assertEquals("DDD怎么写聚合？", q.getTitle().value());
        assertEquals(2, q.getTags().size());
        assertTrue(q.getAnswers().isEmpty());
    }

    @Test
    void shouldAddAnswerInsideQuestionAggregate() {
        Question q = Question.create(1L,1001L, new Title("标题"), new MarkdownBody("内容"), Set.of());
        Answer ans = q.addAnswer(10L, 2002L, new MarkdownBody("回答内容"));
        assertNotNull(ans);
        assertEquals(1, q.getAnswers().size());
    }

    @Test
    void shouldVoteAnswerThroughAggregateRoot() {
        Question q = Question.create(1L,1001L, new Title("标题"), new MarkdownBody("内容"), Set.of());
        Answer ans = q.addAnswer(10L, 2002L, new MarkdownBody("回答"));
        Vote upVote = Vote.create(3003L, true);

        q.voteAnswer(10L, upVote);

        assertEquals(1, ans.getVotes().size());
        assertTrue(ans.getVotes().get(0).isUpVote());
    }

    @Test
    void sameUserReplaceOldVote() {
        Question q = Question.create(1L,1001L, new Title("标题"), new MarkdownBody("内容"), Set.of());
        Answer ans = q.addAnswer(10L,2002L, new MarkdownBody("回答"));
        Vote up = Vote.create(3003L, true);
        Vote down = Vote.create(3003L, false);

        q.voteAnswer(10L, up);
        q.voteAnswer(10L, down);

        assertEquals(1, ans.getVotes().size());
        assertFalse(ans.getVotes().get(0).isUpVote());
    }
}