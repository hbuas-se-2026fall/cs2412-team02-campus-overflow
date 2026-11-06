package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnswerTest {

    @Test
    void createAnswerSuccess() {
        MarkdownBody body = MarkdownBody.ofAnswer("回答内容长度足够满足校验");
        Answer ans = Answer.create(10L, 2002L, body);
        assertNotNull(ans);
        assertEquals(10L, ans.getId());
        assertFalse(ans.isAccepted());
    }

    @Test
    void markAnswerAccepted() {
        MarkdownBody body = MarkdownBody.ofAnswer("回答内容长度足够");
        Answer ans = Answer.create(10L, 2002L, body);
        ans.markAsAccepted();
        assertTrue(ans.isAccepted());
    }

    @Test
    void editAnswerContent() {
        MarkdownBody oldBody = MarkdownBody.ofAnswer("旧的回答内容足够长");
        Answer ans = Answer.create(10L, 2002L, oldBody);
        MarkdownBody newBody = MarkdownBody.ofAnswer("修改后的回答内容长度足够");
        ans.editContent(newBody);
        assertEquals(newBody, ans.getMarkdownBody());
    }
}