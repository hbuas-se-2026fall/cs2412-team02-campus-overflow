package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.UserId;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VoteAndCommentTest {

    @Test
    void upVoteAnswer() {
        var qTitle = new Title("测试对回答进行点赞投票功能是否正常");
        var qContent = new Content("投票场景测试");
        var qAuthor = new UserId("u001");
        var question = Question.create(qTitle, qContent, qAuthor);

        var aContent = new Content("回答内容，准备接收点赞");
        var aAuthor = new UserId("u002");
        var answer = Answer.create(aContent, aAuthor);
        question.addAnswer(answer);

        var voter = new UserId("u005");
        var upVote = new Vote(voter, true);
        answer.vote(upVote);

        assertEquals(1, answer.getVotes().size());
        assertTrue(answer.getVotes().iterator().next().isUp());
    }

    @Test
    void sameUserOnlyOneVote() {
        var qTitle = new Title("同一个用户只能保留一条投票记录校验");
        var qContent = new Content("重复投票覆盖");
        var qAuthor = new UserId("u001");
        var question = Question.create(qTitle, qContent, qAuthor);

        var aContent = new Content("测试回答");
        var aAuthor = new UserId("u002");
        var answer = Answer.create(aContent, aAuthor);
        question.addAnswer(answer);

        var voter = new UserId("u006");
        answer.vote(new Vote(voter, true));
        answer.vote(new Vote(voter, true));

        assertEquals(1, answer.getVotes().size());
    }
}