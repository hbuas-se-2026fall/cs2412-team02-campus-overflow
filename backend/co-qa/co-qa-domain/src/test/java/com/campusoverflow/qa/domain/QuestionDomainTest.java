package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class QuestionDomainTest {

    @Test
    void testCreateQuestion() {
        Title title = new Title("Java开发遇到空指针异常该如何排查解决");
        MarkdownBody content = MarkdownBody.ofQuestion("我在写代码的时候遇到空指针，不知道怎么定位问题，求大佬指点");
        TagName tag = new TagName("java");
        // 只有4个参数：authorId, title, content, tags
        Question q = Question.create(1L, title, content, Set.of(tag));
        assertEquals(1L, q.getAuthorId());
        assertEquals(title, q.getTitle());
    }

    @Test
    void testAddAnswer() {
        Title title = new Title("Java开发遇到空指针异常该如何排查解决");
        MarkdownBody content = MarkdownBody.ofQuestion("我在写代码的时候遇到空指针，不知道怎么定位问题，求大佬指点");
        TagName tag = new TagName("java");
        Question q = Question.create(1L, title, content, Set.of(tag));

        MarkdownBody ansBody = MarkdownBody.ofAnswer("空指针先看哪个变量为null，用断点跟踪");
        // Answer.create(questionId, authorId, body) 3个参数
        Answer ans = Answer.create(1L, 1001L, ansBody);
        q.addAnswer(ans);

        assertEquals(1, q.getAnswers().size());
        assertEquals(ans, q.getAnswers().get(0));
    }

    @Test
    void testAddVote() {
        MarkdownBody ansBody = MarkdownBody.ofAnswer("空指针先看哪个变量为null，用断点跟踪");
        Answer ans = Answer.create(1L, 1001L, ansBody);
        // Vote.create(voterId, targetId, upVote) 3个参数
        Vote v = Vote.create(3003L, 1L, true);
        ans.addVote(v);
        assertEquals(1, ans.getVotes().size());
    }

    @Test
    void testVoteIsUp() {
        Vote v = Vote.create(999L, 100L, false);
        assertFalse(v.isUpVote());
    }
}