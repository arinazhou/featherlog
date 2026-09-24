package com.arinazhou.featherlog.weight;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeightEntryRepository extends JpaRepository<WeightEntry, Long> {

    Page<WeightEntry> findByBirdIdOrderByMeasuredAtDesc(Long birdId, Pageable pageable);

    List<WeightEntry> findByBirdIdAndMeasuredAtAfterOrderByMeasuredAtAsc(Long birdId, Instant after);
}
