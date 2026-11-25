package com.campusoverflow.qa.application.command;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.qa.api.event.ContentDeletedEvent;
import com.campusoverflow.qa.api.event.QuestionEditedEvent;
import com.campusoverflow.qa.api.event.QuestionPostedEvent;
import com.campusoverflow.qa.application.port.ContentRenderer;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.TagRepository;
import com.campusoverflow.qa.domain.Tags;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Title;
import com.campusoverflow.shared.audit.AuditTrail;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.security.Actor;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：发布 / 编辑 / 删除问题。 */
@Service
public class QuestionCommandService {

    static final int PLAIN_TEXT_FOR_INDEX = 2000;

    private final QuestionRepository questions;
    private final TagRepository tags;
    private final IdentityApi identity;
    private final IntegrationEventPublisher events;
    private final ContentRenderer renderer;
    private final QuestionListCache listCache;
    private final AuditTrail audit;
    private final Clock clock;

    public QuestionCommandService(QuestionRepository questions, TagRepository tags, IdentityApi identity,
                                  IntegrationEventPublisher events, ContentRenderer renderer,
                                  QuestionListCache listCache, AuditTrail audit, Clock clock) {
        this.questions = questions;
        this.tags = tags;
        this.identity = identity;
        this.events = events;
        this.renderer = renderer;
        this.listCache = listCache;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public long post(Actor actor, PostQuestionCommand cmd) {
        Instant now = clock.instant();
        Question question = Question.post(actor.userId(), cmd.courseId(), new Title(cmd.title()),
                MarkdownBody.ofQuestion(cmd.body()), Tags.of(cmd.tags()), now);
        Question saved = questions.save(question);

        List<String> tagNames = saved.tags().names();
        tags.adjustUsage(tagNames, 1);
        events.publish(new QuestionPostedEvent(EventIds.next(), now, saved.id(), saved.authorId(),
                saved.courseId(), saved.title().value(),
                renderer.toPlainText(cmd.body(), PLAIN_TEXT_FOR_INDEX), tagNames));
        listCache.invalidateAll();
        return saved.id();
    }

    @Transactional
    public void edit(Actor actor, long questionId, EditQuestionCommand cmd) {
        Instant now = clock.instant();
        Question question = load(questionId);
        boolean manages = identity.canManageCourse(actor.userId(), question.courseId());
        List<String> before = question.tags().names();

        question.edit(actor, manages, new Title(cmd.title()),
                MarkdownBody.ofQuestion(cmd.body()), Tags.of(cmd.tags()), now);
        Question saved = questions.save(question);

        List<String> after = saved.tags().names();
        Set<String> removed = new HashSet<>(before);
        removed.removeAll(after);
        Set<String> added = new HashSet<>(after);
        added.removeAll(before);
        if (!removed.isEmpty()) {
            tags.adjustUsage(removed, -1);
        }
        if (!added.isEmpty()) {
            tags.adjustUsage(added, 1);
        }

        events.publish(new QuestionEditedEvent(EventIds.next(), now, questionId, saved.title().value(),
                renderer.toPlainText(cmd.body(), PLAIN_TEXT_FOR_INDEX), after));
        listCache.invalidateAll();
    }

    @Transactional
    public void delete(Actor actor, long questionId) {
        Instant now = clock.instant();
        Question question = load(questionId);
        boolean manages = identity.canManageCourse(actor.userId(), question.courseId());

        question.softDelete(actor, manages, now);
        questions.save(question);

        tags.adjustUsage(question.tags().names(), -1);
        events.publish(new ContentDeletedEvent(EventIds.next(), now, TargetType.QUESTION.name(),
                questionId, questionId));
        listCache.invalidateAll();
        audit.record("QUESTION_DELETED", actor.userId(), "QUESTION", String.valueOf(questionId),
                question.title().value());
    }

    private Question load(long questionId) {
        return questions.findById(questionId)
                .orElseThrow(() -> new NotFoundException("QA-4001", "问题不存在"));
    }
}
