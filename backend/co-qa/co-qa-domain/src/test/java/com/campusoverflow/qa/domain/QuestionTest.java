package com.campusoverflow.qa.domain;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionTest {

    @Test
    void shouldCreateQuestionSuccess() {
        Title title = new Title("这是一个用于测试的正常问题标题长度足够");
        MarkdownBody content = MarkdownBody.ofQuestion("问题内容描述，用来演示创建问题，长度满足最低要求");
        Long authorUserId = 1L;
        Set<TagName> tags = Set.of(new TagName("java"), new TagName("ddd"));

        Question question = Question.create(1L, authorUserId, title, content, tags);

        assertThat(question).isNotNull();
        assertThat(question.getTitle()).isEqualTo(title);
        assertThat(question.getMarkdownBody()).isEqualTo(content);
        assertThat(question.getAuthorUserId()).isEqualTo(authorUserId);
        assertThat(question.getTags()).containsExactlyInAnyOrder(new TagName("java"), new TagName("ddd"));
    }

    @Test
    void shouldAddAnswerInsideQuestion() {
        Title title = new Title("这是一个用来添加回答的测试问题标题长度足够");
        MarkdownBody content = MarkdownBody.ofQuestion("看看能不能正常新增回答实体，内容长度满足要求");
        Long authorUserId = 1L;
        Set<TagName> tags = Set.of();
        Question question = Question.create(1L, authorUserId, title, content, tags);

        MarkdownBody answerContent = MarkdownBody.ofAnswer("这是一条测试回答的文本内容，长度足够");
        Long answerAuthorId = 2L;
        Answer answer = question.addAnswer(1001L, answerAuthorId, answerContent);

        assertThat(question.getAnswers()).contains(answer);
    }

    @Test
    void sameUserReplaceOldVoteOnAnswer() {
        Title title = new Title("测试同一个用户重复投票会替换旧投票标题足够长");
        MarkdownBody content = MarkdownBody.ofQuestion("投票替换逻辑校验，内容长度满足最低要求");
        Long authorUserId = 1L;
        Set<TagName> tags = Set.of();
        Question question = Question.create(1L, authorUserId, title, content, tags);

        MarkdownBody answerContent = MarkdownBody.ofAnswer("回答内容长度足够满足校验规则");
        Long answerAuthorId = 2L;
        Answer answer = question.addAnswer(1001L, answerAuthorId, answerContent);

        Long voterId = 3L;
        Vote voteUp = Vote.create(voterId, true);
        Vote voteDown = Vote.create(voterId, false);

        answer.addVote(voteUp);
        answer.addVote(voteDown);

        assertThat(answer.getVotes()).hasSize(1);
        assertThat(answer.getVotes().iterator().next().isUpVote()).isFalse();
    }

    @Test
    void shouldVoteAnswerThroughAggregateRoot() {
        Title title = new Title("通过聚合根Question给Answer执行投票操作标题足够长");
        MarkdownBody content = MarkdownBody.ofQuestion("聚合根转发投票逻辑，内容长度满足要求");
        Long authorUserId = 1L;
        Set<TagName> tags = Set.of();
        Question question = Question.create(1L, authorUserId, title, content, tags);

        MarkdownBody answerContent = MarkdownBody.ofAnswer("待投票的回答内容长度足够满足校验");
        Long answerAuthorId = 2L;
        Answer answer = question.addAnswer(1001L, answerAuthorId, answerContent);

        Long voterId = 4L;
        Vote vote = Vote.create(voterId, true);
        question.voteAnswer(1001L, vote);

        assertThat(answer.getVotes()).contains(vote);
    }

    @Test
    void shouldVoteQuestionItself() {
        Title title = new Title("测试给问题本身投票标题长度足够满足要求");
        MarkdownBody content = MarkdownBody.ofQuestion("问题投票逻辑，内容长度满足最低校验要求");
        Long authorUserId = 1L;
        Set<TagName> tags = Set.of();
        Question question = Question.create(1L, authorUserId, title, content, tags);

        Long voterId = 5L;
        Vote vote = Vote.create(voterId, true);
        question.voteQuestion(vote);

        assertThat(question.getVotes()).hasSize(1);
    }

    @Test
    void shouldFailWhenAuthorIdInvalid() {
        Title title = new Title("标题足够长不会触发长度校验报错");
        MarkdownBody content = MarkdownBody.ofQuestion("合法内容足够长满足最低字符数要求");
        Set<TagName> tags = Set.of();
        assertThatThrownBy(() -> Question.create(1L, -1L, title, content, tags))
                .isInstanceOf(IllegalArgumentException.class);
    }
}