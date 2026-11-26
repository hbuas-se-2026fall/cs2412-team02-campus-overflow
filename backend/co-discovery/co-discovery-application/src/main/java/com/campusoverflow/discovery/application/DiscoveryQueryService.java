package com.campusoverflow.discovery.application;

import com.campusoverflow.discovery.domain.search.RelatedQuestionPolicy;
import com.campusoverflow.discovery.domain.search.SearchHit;
import com.campusoverflow.discovery.domain.search.SearchIndex;
import com.campusoverflow.discovery.domain.search.SearchPort;
import com.campusoverflow.discovery.domain.search.SearchQuery;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.shared.page.PageResult;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：搜索与筛选、相关问题推荐。 */
@Service
@Transactional(readOnly = true)
public class DiscoveryQueryService {

    /** 候选放大倍数：先多取一些，交给策略层排序后再截断，避免只按共享标签数排序丢掉高分候选。 */
    private static final int CANDIDATE_FACTOR = 3;

    private final SearchPort search;
    private final SearchIndex index;
    private final IdentityApi identity;

    public DiscoveryQueryService(SearchPort search, SearchIndex index, IdentityApi identity) {
        this.search = search;
        this.index = index;
        this.identity = identity;
    }

    public PageResult<SearchResultView> search(SearchQuery query) {
        PageResult<SearchHit> hits = search.search(query);
        if (hits.items().isEmpty()) {
            return new PageResult<>(List.of(), hits.total(), hits.page(), hits.size());
        }
        // 批量取作者信息：命中页内只发一次查询，避免逐条 N+1
        Map<Long, UserSummary> authors = identity.findUsers(hits.items().stream().map(SearchHit::authorId).toList());
        return hits.map(hit -> toView(hit, authors.get(hit.authorId())));
    }

    public List<RelatedQuestionView> related(long questionId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        List<String> tags = index.findTags(questionId);
        if (tags.isEmpty()) {
            return List.of();
        }
        int ownTagCount = tags.size();
        List<SearchIndex.RelatedCandidate> candidates =
                index.findCandidatesSharingTags(questionId, tags, limit * CANDIDATE_FACTOR);
        return RelatedQuestionPolicy.rank(candidates, ownTagCount, limit).stream()
                .map(c -> new RelatedQuestionView(c.questionId(), c.title(), c.answerCount(), c.accepted(),
                        RelatedQuestionPolicy.jaccard(c.sharedTags(), c.tagCount(), ownTagCount)))
                .toList();
    }

    private static SearchResultView toView(SearchHit hit, UserSummary author) {
        SearchResultView.Author view = author == null
                ? new SearchResultView.Author(hit.authorId(), "已注销用户", "STUDENT", false)
                : new SearchResultView.Author(author.id(), author.displayName(), author.role().name(),
                author.verified());
        return new SearchResultView(hit.questionId(), hit.title(), hit.snippet(), hit.tags(), hit.courseId(), view,
                hit.score(), hit.answerCount(), hit.accepted(), hit.bountyOpen(), hit.createdAt());
    }
}
