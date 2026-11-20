package com.campusoverflow.qa.application.query;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.application.port.ContentRenderer;
import com.campusoverflow.qa.application.port.QaReadModel;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.application.port.ViewCounter;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 问答查询用例（读路径）。 */
@Service
@Transactional(readOnly = true)
public class QuestionQueryService {

    private static final int EXCERPT_LENGTH = 160;

    private final QaReadModel readModel;
    private final IdentityApi identity;
    private final ContentRenderer renderer;
    private final ViewCounter viewCounter;
    private final QuestionListCache cache;

    public QuestionQueryService(QaReadModel readModel, IdentityApi identity, ContentRenderer renderer,
                                ViewCounter viewCounter, QuestionListCache cache) {
        this.readModel = readModel;
        this.identity = identity;
        this.renderer = renderer;
        this.viewCounter = viewCounter;
        this.cache = cache;
    }

    public PageResult<QuestionSummaryView> list(Long courseId, PageRequest page) {
        return cache.get(courseId, page).orElseGet(() -> {
            PageResult<QuestionSummaryView> view = readModel.listQuestions(courseId, page).map(this::toSummary);
            cache.put(courseId, page, view);
            return view;
        });
    }

    public QuestionDetailView detail(long questionId, Actor actorOrNull) {
        QuestionRow question = readModel.findQuestion(questionId)
                .orElseThrow(() -> new NotFoundException("QA-4001", "问题不存在"));
        viewCounter.increment(questionId);

        List<AnswerRow> answers = readModel.findAnswers(questionId);
        List<CommentRow> comments = readModel.findComments(questionId);

        Set<Long> userIds = new HashSet<>();
        userIds.add(question.authorId());
        answers.forEach(a -> {
            userIds.add(a.authorId());
            if (a.endorsedBy() != null) {
                userIds.add(a.endorsedBy());
            }
        });
        comments.forEach(c -> userIds.add(c.authorId()));
        Map<Long, UserSummary> users = identity.findUsers(userIds);

        Long courseId = question.courseId();
        boolean canManage = actorOrNull != null && identity.canManageCourse(actorOrNull.userId(), courseId);
        boolean isAuthor = actorOrNull != null && actorOrNull.is(question.authorId());
        PermissionsView permissions = actorOrNull == null ? PermissionsView.NONE
                : new PermissionsView(isAuthor || canManage, isAuthor || canManage,
                (isAuthor || canManage) && question.acceptedAnswerId() == null, canManage);

        CourseRefView course = courseId == null ? null
                : identity.findCourse(courseId)
                .map(c -> new CourseRefView(c.id(), c.code(), c.name())).orElse(null);

        Map<String, Integer> myVotes = actorOrNull == null ? Map.of()
                : readModel.findMyVotes(actorOrNull.userId(), questionId,
                answers.stream().map(AnswerRow::id).toList());

        List<CommentView> questionComments = comments.stream()
                .filter(c -> "QUESTION".equals(c.targetType()) && c.targetId() == questionId)
                .map(c -> toCommentView(c, users))
                .toList();

        Map<Long, List<CommentView>> answerComments = comments.stream()
                .filter(c -> "ANSWER".equals(c.targetType()))
                .collect(Collectors.groupingBy(CommentRow::targetId,
                        Collectors.mapping(c -> toCommentView(c, users), Collectors.toList())));

        Long accepted = question.acceptedAnswerId();
        List<AnswerView> answerViews = answers.stream()
                .sorted(Comparator.comparingInt((AnswerRow a) -> accepted != null && accepted == a.id() ? 1 : 0)
                        .reversed()
                        .thenComparing(Comparator.comparingInt(AnswerRow::score).reversed()))
                .map(a -> new AnswerView(a.id(), a.body(), renderer.toSafeHtml(a.body()),
                        AuthorView.of(a.authorId(), users.get(a.authorId())), a.score(),
                        accepted != null && accepted == a.id(),
                        a.endorsedBy() == null ? null : AuthorView.of(a.endorsedBy(), users.get(a.endorsedBy())),
                        myVotes.getOrDefault("ANSWER:" + a.id(), 0),
                        actorOrNull != null && (actorOrNull.is(a.authorId()) || canManage),
                        a.createdAt(), a.updatedAt(),
                        answerComments.getOrDefault(a.id(), List.of())))
                .toList();

        return new QuestionDetailView(question.id(), question.title(), question.body(),
                renderer.toSafeHtml(question.body()), question.tags(), course,
                AuthorView.of(question.authorId(), users.get(question.authorId())),
                question.score(), question.answerCount(), question.viewCount(), accepted,
                myVotes.getOrDefault("QUESTION:" + questionId, 0),
                question.createdAt(), question.updatedAt(), permissions, questionComments, answerViews);
    }

    public List<TagView> popularTags(int limit) {
        return readModel.popularTags(limit);
    }

    private QuestionSummaryView toSummary(QuestionRow row) {
        return new QuestionSummaryView(row.id(), row.title(),
                renderer.toPlainText(row.body(), EXCERPT_LENGTH), row.tags(), row.courseId(),
                AuthorView.of(row.authorId(), identity.findUser(row.authorId()).orElse(null)),
                row.score(), row.answerCount(), row.viewCount(), row.acceptedAnswerId() != null, row.createdAt());
    }

    private CommentView toCommentView(CommentRow row, Map<Long, UserSummary> users) {
        return new CommentView(row.id(), AuthorView.of(row.authorId(), users.get(row.authorId())),
                row.parentId(), row.body(), row.createdAt());
    }
}
