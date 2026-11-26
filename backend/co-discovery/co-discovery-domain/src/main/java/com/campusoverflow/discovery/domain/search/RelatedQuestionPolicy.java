package com.campusoverflow.discovery.domain.search;

import java.util.Comparator;
import java.util.List;

/**
 * 相关问题推荐（规则引擎，非 AI）：按标签集合的 Jaccard 相似度排序，
 * J(A,B) = |A∩B| / |A∪B|；相似度相同则得分高、已采纳者优先。
 */
public final class RelatedQuestionPolicy {

    private RelatedQuestionPolicy() {
    }

    public static double jaccard(int shared, int sizeA, int sizeB) {
        int union = sizeA + sizeB - shared;
        if (shared <= 0 || union <= 0) {
            return 0d;
        }
        return (double) shared / union;
    }

    /**
     * 按相似度降序排序，同分时得分高者优先，再按已采纳优先，最后用问题 ID 保证结果稳定。
     */
    public static List<SearchIndex.RelatedCandidate> rank(List<SearchIndex.RelatedCandidate> candidates, int ownTagCount,
                                                          int limit) {
        if (candidates == null || candidates.isEmpty() || limit <= 0) {
            return List.of();
        }
        return candidates.stream()
                .filter(c -> c.sharedTags() > 0)
                .sorted(Comparator.comparingDouble(
                                (SearchIndex.RelatedCandidate c) -> jaccard(c.sharedTags(), c.tagCount(), ownTagCount))
                        .reversed()
                        .thenComparing(Comparator.comparingInt(SearchIndex.RelatedCandidate::score).reversed())
                        .thenComparing(c -> !c.accepted())
                        .thenComparingLong(SearchIndex.RelatedCandidate::questionId))
                .limit(limit)
                .toList();
    }
}
