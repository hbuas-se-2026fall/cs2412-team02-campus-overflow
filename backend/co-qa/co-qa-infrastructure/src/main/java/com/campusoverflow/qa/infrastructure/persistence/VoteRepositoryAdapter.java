package com.campusoverflow.qa.infrastructure.persistence;

import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Vote;
import com.campusoverflow.qa.domain.VoteRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class VoteRepositoryAdapter implements VoteRepository {

    private final VoteJpaRepository jpa;

    public VoteRepositoryAdapter(VoteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Vote> find(long voterId, TargetType targetType, long targetId) {
        return jpa.findByVoterIdAndTargetTypeAndTargetId(voterId, targetType.name(), targetId)
                .map(e -> Vote.reconstitute(
                        e.getId(),
                        e.getVoterId(),
                        e.getTargetId(),
                        e.isUpVote()
                ));
    }

    @Override
    public Vote save(Vote v) {
        if (v.getId() == null) {
            // 新增，这里注意：需要上层调用处传入targetType，当前Vote领域对象不带targetType
            // 如果你需要在这里存targetType，要改Vote领域类
            VoteJpaEntity entity = new VoteJpaEntity(v.getVoterId(), null, v.getTargetId(), v.isUpVote());
            VoteJpaEntity saved = jpa.save(entity);
            return Vote.reconstitute(saved.getId(), saved.getVoterId(), saved.getTargetId(), saved.isUpVote());
        } else {
            VoteJpaEntity entity = jpa.findById(v.getId()).orElseThrow();
            entity.setUpVote(v.isUpVote());
            VoteJpaEntity saved = jpa.save(entity);
            return Vote.reconstitute(saved.getId(), saved.getVoterId(), saved.getTargetId(), saved.isUpVote());
        }
    }

    @Override
    public void delete(Vote v) {
        if (v.getId() != null) {
            jpa.deleteById(v.getId());
        }
    }
}