package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class QuestionDomainTest {

    @Test
    void testCreateQuestion() {
        Title title = new Title("Java开发遇到空指针异常该如何排查解决");
        MarkdownBody content = new MarkdownBody("我在写代码的时候遇到空指针，不知道怎么定位问题，求大佬指点");
        TagName tag = new TagName("java");
        Question q = Question.create(1L, 1001L, title, content, Set.of(tag));
        assertEquals(1L, q.getId());
        assertEquals(title, q.getTitle());
    }

    @Test
    void testAddAnswer() {
        Title title = new Title("Java开发遇到空指针异常该如何排查解决");
        MarkdownBody content = new MarkdownBody("我在写代码的时候遇到空指针，不知道怎么定位问题，求大佬指点");
        TagName tag = new TagName("java");
        Question q = Question.create(1L, 1001L, title, content, Set.of(tag));

        MarkdownBody ansBody = new MarkdownBody("可以先看报错堆栈，找到哪一行变量为null，增加判空逻辑");
        q.addAnswer(10L, 2002L, ansBody);
        assertEquals(1, q.getAnswers().size());
    }

    @Test
    void testAddVote() {
        MarkdownBody ansBody = new MarkdownBody("可以先看报错堆栈，找到哪一行变量为null，增加判空逻辑");
        Answer ans = Answer.create(10L, 2002L, ansBody);
        Vote v = Vote.create(3003L, true);
        ans.addVote(v);
        assertEquals(1, ans.getVotes().size());
    }

    @Test
    void testVoteIsUp() {
        Vote v = Vote.create(999L, false);
        assertFalse(v.isUpVote());
    }
}