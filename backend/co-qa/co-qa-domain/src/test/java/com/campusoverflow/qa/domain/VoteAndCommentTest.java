package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class VoteAndCommentTest {
    @Test
    void upVoteAnswer() {
        Question q = Question.create(1L,1001L, new Title("标题"), new MarkdownBody("内容"), Set.of());
        Answer ans = q.addAnswer(10L,2002L, new MarkdownBody("回答"));
        Vote upVote = Vote.create(3003L, true);
        q.voteAnswer(10L, upVote);
        assertEquals(1, ans.getVotes().size());
        assertTrue(ans.getVotes().get(0).isUpVote());
    }

    @Test
    void sameUserOnlyOneVote() {
        Question q = Question.create(1L,1001L, new Title("标题"), new MarkdownBody("内容"), Set.of());
        Answer ans = q.addAnswer(10L,2002L, new MarkdownBody("回答"));
        Vote upVote = Vote.create(3003L, true);
        Vote downVote = Vote.create(3003L, false);
        q.voteAnswer(10L, upVote);
        q.voteAnswer(10L, downVote);
        assertEquals(1, ans.getVotes().size());
        assertFalse(ans.getVotes().get(0).isUpVote());
    }
}