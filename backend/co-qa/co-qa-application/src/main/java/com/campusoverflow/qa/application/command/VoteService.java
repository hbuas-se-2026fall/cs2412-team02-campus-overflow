package com.campusoverflow.qa.application.command;

import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.qa.domain.Answer;
import com.campusoverflow.qa.domain.AnswerRepository;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Vote;
import com.campusoverflow.qa.domain.VoteRepository;
import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.security.Actor;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用例：投票 / 改投 / 撤销（value ∈ {-1, 0, 1}，0 表示撤销）。
 * 发布的事件同时携带旧值与新值，下游据此计算声誉增量。
 */
@Service
public class VoteService {

    private final VoteRepository votes;
    private final QuestionRepository questions;
    private final AnswerRepository answers;
    private final IntegrationEventPublisher events;
    private final Clock clock;

    public VoteService(VoteRepository votes, QuestionRepository questions, AnswerRepository answers,
                       IntegrationEventPublisher events, Clock clock) {
        this.votes = votes;
        this.questions = questions;
        this.answers = answers;
        this.events = events;
        this.clock = clock;
    }

    public record VoteResult(int myVote, int scoreDelta) {
    }

    private record Target(long authorId, long questionId) {
    }

    @Transactional
    public VoteResult cast(Actor actor, TargetType type, long targetId, int value) {
        if (value != -1 && value != 0 && value != 1) {
            throw new BusinessRuleException("QA-3002", "投票值只能是 -1、0 或 1");
        }
        Target target = resolve(type, targetId);
        if (target.authorId() == actor.userId()) {
            throw new BusinessRuleException("QA-3001", "不能给自己的内容投票");
        }

        Instant now = clock.instant();
        Optional<Vote> existing = votes.find(actor.userId(), type, targetId);
        int oldValue = existing.map(Vote::value).orElse(0);

        if (value == 0) {
            if (existing.isEmpty()) {
                return new VoteResult(0, 0);
            }
            votes.delete(existing.get());
            int delta = -oldValue;
            applyScore(type, targetId, delta);
            publish(type, targetId, target, actor, oldValue, 0, now);
            return new VoteResult(0, delta);
        }

        int delta = value - oldValue;
        if (delta == 0) {
            // 重复投同一票：幂等，不落库也不发事件
            return new VoteResult(value, 0);
        }
        Vote vote;
        if (existing.isPresent()) {
            vote = existing.get();
            vote.change(value, now);
        } else {
            vote = Vote.cast(actor.userId(), target.authorId(), type, targetId, value, now);
        }
        votes.save(vote);
        applyScore(type, targetId, delta);
        publish(type, targetId, target, actor, oldValue, value, now);
        return new VoteResult(value, delta);
    }

    private void publish(TargetType type, long targetId, Target target, Actor actor, int oldValue, int newValue,
                         Instant now) {
        events.publish(new VoteCastEvent(EventIds.next(), now, type.name(), targetId, target.authorId(),
                actor.userId(), target.questionId(), oldValue, newValue));
    }

    private void applyScore(TargetType type, long targetId, int delta) {
        if (delta == 0) {
            return;
        }
        if (type == TargetType.QUESTION) {
            questions.adjustScore(targetId, delta);
        } else {
            answers.adjustScore(targetId, delta);
        }
    }

    private Target resolve(TargetType type, long targetId) {
        if (type == TargetType.QUESTION) {
            Question question = questions.findById(targetId)
                    .orElseThrow(() -> new NotFoundException("QA-4001", "问题不存在"));
            return new Target(question.authorId(), question.id());
        }
        Answer answer = answers.findById(targetId)
                .orElseThrow(() -> new NotFoundException("QA-4002", "回答不存在"));
        return new Target(answer.authorId(), answer.questionId());
    }
}
