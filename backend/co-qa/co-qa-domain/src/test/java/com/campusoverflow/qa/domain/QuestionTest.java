package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class QuestionTest {

    @Test
    void shouldCreateQuestionSuccess() {
        Title title = new Title("这是一个测试标题，用来测试问题创建");
        MarkdownBody content = MarkdownBody.ofQuestion("这是问题正文内容，用来测试领域对象");
        Long authorId = 1L;
        Set<TagName> tags = Set.of(new TagName("java"));

        Question question = Question.create(authorId, title, content, tags);

        assertThat(question).isNotNull();
        assertThat(question.getTitle()).isEqualTo(title);
        assertThat(question.getContent()).isEqualTo(content);
        assertThat(question.getAuthorId()).isEqualTo(authorId);
        assertThat(question.getTags()).containsAll(tags);
    }

    @Test
    void shouldAddAnswerInsideQuestion() {
        Title title = new Title("这是一个测试标题，用来测试问题创建");
        MarkdownBody content = MarkdownBody.ofQuestion("这是问题正文内容，用来测试领域对象");
        Long authorId = 1L;
        Set<TagName> tags = Set.of(new TagName("java"));
        Question question = Question.create(authorId, title, content, tags);

        MarkdownBody ansBody = MarkdownBody.ofAnswer("这是回答内容");
        Answer answer = Answer.create(1L, 2L, ansBody);
        question.addAnswer(answer);

        assertThat(question.getAnswers()).hasSize(1);
        assertThat(question.getAnswers().get(0)).isEqualTo(answer);
    }
}