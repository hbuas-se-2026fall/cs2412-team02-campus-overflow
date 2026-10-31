package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnswerTest {
    @Test
    void createAnswerSuccess() {
        Answer ans = Answer.create(10L, 2002L, new MarkdownBody("回答内容"));
        assertNotNull(ans);
        assertEquals(10L, ans.getId());
        assertFalse(ans.isAccepted());
    }

    @Test
    void markAnswerAccepted() {
        Answer ans = Answer.create(10L, 2002L, new MarkdownBody("回答内容"));
        ans.markAsAccepted();
        assertTrue(ans.isAccepted());
    }

    @Test
    void editAnswerContent() {
        Answer ans = Answer.create(10L, 2002L, new MarkdownBody("旧回答"));
        ans.editContent(new MarkdownBody("修改后的回答"));
        assertEquals("修改后的回答", ans.getMarkdownBody().value());
    }
}