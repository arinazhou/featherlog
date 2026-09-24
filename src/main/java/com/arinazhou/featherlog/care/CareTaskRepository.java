package com.arinazhou.featherlog.care;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CareTaskRepository extends JpaRepository<CareTask, Long> {

    List<CareTask> findByBirdIdAndActiveTrueOrderByNextDueAtAsc(Long birdId);

    Optional<CareTask> findByIdAndBirdIdAndActiveTrue(Long id, Long birdId);

    @Query("select t from CareTask t join fetch t.bird where t.active = true and t.nextDueAt < :now")
    List<CareTask> findOverdue(@Param("now") Instant now);
}
