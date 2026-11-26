package com.campusoverflow.discovery.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusoverflow.discovery.domain.search.SearchDocument;
import com.campusoverflow.discovery.domain.search.SearchHit;
import com.campusoverflow.discovery.domain.search.SearchIndex;
import com.campusoverflow.discovery.domain.search.SearchPort;
import com.campusoverflow.discovery.domain.search.SearchQuery;
import com.campusoverflow.identity.api.CourseSummary;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.ContentDeletedEvent;
import com.campusoverflow.qa.api.event.QuestionEditedEvent;
import com.campusoverflow.qa.api.event.QuestionPostedEvent;
import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.reputation.api.event.BountyAwardedEvent;
import com.campusoverflow.reputation.api.event.BountyClosedEvent;
import com.campusoverflow.reputation.api.event.BountyOpenedEvent;
import com.campusoverflow.shared.event.ProcessedEventStore;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** 应用层用例：事件投影（幂等）与查询编排（Fake 替身，不需要数据库与 Spring 容器）。 */
class DiscoveryApplicationTest {

    private static final Instant AT = Instant.parse("2026-11-26T00:00:00Z");

    /* ============================ 投影器 ============================ */

    @Test
    void duplicateDeliveryIsIgnored() {
        FakeIndex index = new FakeIndex();
        FakeStore store = new FakeStore();
        SearchIndexProjector projector = new SearchIndexProjector(index, store);

        QuestionPostedEvent event = new QuestionPostedEvent("evt-1", AT, 10L, 1L, 2L, "红黑树怎么旋转",
                "红黑树旋转的四种情况", List.of("算法", "java"));

        projector.on(event);
        projector.on(event);

        assertThat(index.inserted).hasSize(1);
        assertThat(store.seen).containsExactly("evt-1");
    }

    @Test
    void questionPostingFoldsIntoSearchDocument() {
        FakeIndex index = new FakeIndex();
        SearchIndexProjector projector = new SearchIndexProjector(index, new FakeStore());

        projector.on(new QuestionPostedEvent("evt-1", AT, 10L, 1L, 2L, "标题", "正文", List.of("tag")));

        SearchDocument doc = index.inserted.get(0);
        assertThat(doc.questionId()).isEqualTo(10L);
        assertThat(doc.authorId()).isEqualTo(1L);
        assertThat(doc.courseId()).isEqualTo(2L);
        assertThat(doc.tags()).containsExactly("tag");
        assertThat(doc.createdAt()).isEqualTo(AT);
    }

    @Test
    void editingRewritesTextAndTags() {
        FakeIndex index = new FakeIndex();
        SearchIndexProjector projector = new SearchIndexProjector(index, new FakeStore());

        projector.on(new QuestionEditedEvent("evt-2", AT, 10L, "新标题", "新正文", List.of("a", "b")));

        assertThat(index.textUpdates).containsExactly("10|新标题|新正文|a,b");
    }

    @Test
    void answerSubmissionAndDeletionAdjustCounters() {
        FakeIndex index = new FakeIndex();
        SearchIndexProjector projector = new SearchIndexProjector(index, new FakeStore());

        projector.on(new AnswerSubmittedEvent("evt-3", AT, 100L, 10L, 5L, 1L, "标题"));
        projector.on(new ContentDeletedEvent("evt-4", AT, "ANSWER", 100L, 10L));

        assertThat(index.answerDeltas).containsExactly(1, -1);
    }

    @Test
    void deletingQuestionMarksIndexDeleted() {
        FakeIndex index = new FakeIndex();
        SearchIndexProjector projector = new SearchIndexProjector(index, new FakeStore());

        projector.on(new ContentDeletedEvent("evt-5", AT, "QUESTION", 10L, 10L));

        assertThat(index.deleted).containsExactly(10L);
    }

    @Test
    void acceptingAnswerMarksAccepted() {
        FakeIndex index = new FakeIndex();
        SearchIndexProjector projector = new SearchIndexProjector(index, new FakeStore());

        projector.on(new AnswerAcceptedEvent("evt-6", AT, 100L, 10L, 5L, 1L, 1L, "标题"));

        assertThat(index.accepted).containsExactly(10L);
    }

    @Test
    void voteDeltaHandlesSwitchingFromUpToDown() {
        FakeIndex index = new FakeIndex();
        SearchIndexProjector projector = new SearchIndexProjector(index, new FakeStore());

        projector.on(new VoteCastEvent("evt-7", AT, "QUESTION", 10L, 1L, 2L, 10L, 1, -1));

        assertThat(index.scoreDeltas).containsExactly(-2);
    }

    @Test
    void bountyLifecycleTogglesFlag() {
        FakeIndex index = new FakeIndex();
        SearchIndexProjector projector = new SearchIndexProjector(index, new FakeStore());

        projector.on(new BountyOpenedEvent("evt-8", AT, 200L, 10L, 1L, 30, AT.plusSeconds(3600)));
        projector.on(new BountyAwardedEvent("evt-9", AT, 200L, 10L, 1L, 5L, 30));
        projector.on(new BountyClosedEvent("evt-10", AT, 200L, 10L, 1L, 30));

        assertThat(index.bountyFlags).containsExactly(true, false, false);
    }

    /* ============================ 查询服务 ============================ */

    @Test
    void searchMapsHitsAndEnrichesAuthor() {
        SearchHit hit = new SearchHit(10L, "红黑树怎么旋转", "四种情况...", List.of("算法"), 2L, 1L, 5, 3, true, false, AT);
        FakePort port = new FakePort(new PageResult<>(List.of(hit), 1, 1, 20));
        FakeIdentity identity = new FakeIdentity(Map.of(1L, new UserSummary(1L, "王老师", Role.TEACHER, true, "计算机")));
        DiscoveryQueryService service = new DiscoveryQueryService(port, new FakeIndex(), identity);

        PageResult<SearchResultView> result = service.search(
                new SearchQuery("红黑树", null, null, null, false, false, new PageRequest(1, 20)));

        assertThat(result.total()).isEqualTo(1);
        SearchResultView view = result.items().get(0);
        assertThat(view.title()).isEqualTo("红黑树怎么旋转");
        assertThat(view.excerpt()).isEqualTo("四种情况...");
        assertThat(view.tags()).containsExactly("算法");
        assertThat(view.author().displayName()).isEqualTo("王老师");
        assertThat(view.author().role()).isEqualTo("TEACHER");
        assertThat(view.author().verified()).isTrue();
        assertThat(identity.lastIds).containsExactly(1L);
    }

    @Test
    void emptySearchDoesNotQueryIdentity() {
        FakePort port = new FakePort(new PageResult<>(List.of(), 0, 1, 20));
        FakeIdentity identity = new FakeIdentity(Map.of());
        DiscoveryQueryService service = new DiscoveryQueryService(port, new FakeIndex(), identity);

        PageResult<SearchResultView> result = service.search(new SearchQuery(null, null, null, null, false, false, null));

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
        assertThat(identity.lastIds).isNull();
    }

    @Test
    void relatedRanksByJaccardAndExposesSimilarity() {
        FakeIndex index = new FakeIndex();
        index.tags = List.of("算法", "java", "树");
        index.candidates = List.of(
                new SearchIndex.RelatedCandidate(2L, "AVL 树", 2, 2, 0, 1, false),
                new SearchIndex.RelatedCandidate(3L, "红黑树插入", 2, 3, 5, 4, true),
                new SearchIndex.RelatedCandidate(4L, "无关问题", 0, 2, 99, 9, false));
        DiscoveryQueryService service = new DiscoveryQueryService(new FakePort(null), index,
                new FakeIdentity(Map.of()));

        List<RelatedQuestionView> related = service.related(10L, 2);

        assertThat(related).extracting(RelatedQuestionView::id).containsExactly(2L, 3L);
        assertThat(related.get(0).similarity()).isEqualTo(2.0 / 3);
        assertThat(related.get(1).similarity()).isEqualTo(0.5);
        assertThat(index.requestedLimit).isEqualTo(6);
    }

    @Test
    void relatedWithoutTagsReturnsEmpty() {
        FakeIndex index = new FakeIndex();
        DiscoveryQueryService service = new DiscoveryQueryService(new FakePort(null), index,
                new FakeIdentity(Map.of()));

        assertThat(service.related(10L, 5)).isEmpty();
        assertThat(index.requestedLimit).isZero();
    }

    /* ============================ Fake 替身 ============================ */

    private static class FakeIndex implements SearchIndex {

        private final List<SearchDocument> inserted = new ArrayList<>();
        private final List<String> textUpdates = new ArrayList<>();
        private final List<Integer> answerDeltas = new ArrayList<>();
        private final List<Integer> scoreDeltas = new ArrayList<>();
        private final List<Long> accepted = new ArrayList<>();
        private final List<Long> deleted = new ArrayList<>();
        private final List<Boolean> bountyFlags = new ArrayList<>();
        private List<String> tags = List.of();
        private List<RelatedCandidate> candidates = List.of();
        private int requestedLimit;

        @Override
        public void insert(SearchDocument document) {
            inserted.add(document);
        }

        @Override
        public void updateText(long questionId, String title, String bodyText, List<String> tags, Instant at) {
            textUpdates.add(questionId + "|" + title + "|" + bodyText + "|" + String.join(",", tags));
        }

        @Override
        public void adjustAnswerCount(long questionId, int delta, Instant at) {
            answerDeltas.add(delta);
        }

        @Override
        public void adjustScore(long questionId, int delta) {
            scoreDeltas.add(delta);
        }

        @Override
        public void markAccepted(long questionId, Instant at) {
            accepted.add(questionId);
        }

        @Override
        public void setBountyOpen(long questionId, boolean open) {
            bountyFlags.add(open);
        }

        @Override
        public void markDeleted(long questionId) {
            deleted.add(questionId);
        }

        @Override
        public List<String> findTags(long questionId) {
            return tags;
        }

        @Override
        public List<RelatedCandidate> findCandidatesSharingTags(long questionId, List<String> tags, int limit) {
            requestedLimit = limit;
            return candidates;
        }
    }

    private static class FakeStore implements ProcessedEventStore {

        private final Set<String> seen = new HashSet<>();

        @Override
        public boolean markProcessed(String eventId, String consumer) {
            return seen.add(eventId);
        }
    }

    private static class FakePort implements SearchPort {

        private final PageResult<SearchHit> result;

        private FakePort(PageResult<SearchHit> result) {
            this.result = result;
        }

        @Override
        public PageResult<SearchHit> search(SearchQuery query) {
            return result;
        }
    }

    private static class FakeIdentity implements IdentityApi {

        private final Map<Long, UserSummary> users;
        private Collection<Long> lastIds;

        private FakeIdentity(Map<Long, UserSummary> users) {
            this.users = users;
        }

        @Override
        public Optional<UserSummary> findUser(long userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public Map<Long, UserSummary> findUsers(Collection<Long> userIds) {
            lastIds = userIds;
            Map<Long, UserSummary> found = new HashMap<>();
            userIds.forEach(id -> {
                UserSummary user = users.get(id);
                if (user != null) {
                    found.put(id, user);
                }
            });
            return found;
        }

        @Override
        public Optional<UserSummary> findByDisplayName(String displayName) {
            return users.values().stream().filter(u -> u.displayName().equals(displayName)).findFirst();
        }

        @Override
        public Optional<CourseSummary> findCourse(long courseId) {
            return Optional.empty();
        }

        @Override
        public boolean canManageCourse(long userId, Long courseId) {
            return false;
        }
    }
}
