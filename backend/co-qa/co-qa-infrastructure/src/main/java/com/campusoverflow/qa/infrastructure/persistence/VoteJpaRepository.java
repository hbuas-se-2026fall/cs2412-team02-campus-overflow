package com.campusoverflow.qa.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, Long> {
    Optional<VoteJpaEntity> findByVoterIdAndTargetTypeAndTargetId(long voterId, String targetType, long targetId);
}