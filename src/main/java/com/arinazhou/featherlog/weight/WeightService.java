package com.arinazhou.featherlog.weight;

import com.arinazhou.featherlog.alert.AlertResponse;
import com.arinazhou.featherlog.alert.AlertService;
import com.arinazhou.featherlog.bird.Bird;
import com.arinazhou.featherlog.weight.WeightDtos.WeightLogResult;
import com.arinazhou.featherlog.weight.WeightDtos.WeightRequest;
import com.arinazhou.featherlog.weight.WeightDtos.WeightResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeightService {

    private final WeightEntryRepository weights;
    private final WeightTrendAnalyzer analyzer;
    private final AlertService alertService;
    private final Clock clock;

    public WeightService(WeightEntryRepository weights, WeightTrendAnalyzer analyzer, AlertService alertService,
                         Clock clock) {
        this.weights = weights;
        this.analyzer = analyzer;
        this.alertService = alertService;
        this.clock = clock;
    }

    @Transactional
    public WeightLogResult record(Bird bird, WeightRequest request) {
        Instant measuredAt = request.measuredAt() == null ? clock.instant() : request.measuredAt();
        WeightEntry saved = weights.save(new WeightEntry(bird, request.grams(), measuredAt, request.note()));

        WeightAssessment assessment = assess(bird);
        List<AlertResponse> raised = assessment.findings().stream()
                .map(f -> alertService.raise(bird, f.type(), f.severity(), f.message()))
                .flatMap(Optional::stream)
                .map(AlertResponse::from)
                .toList();
        return new WeightLogResult(WeightResponse.from(saved), assessment, raised);
    }

    @Transactional(readOnly = true)
    public WeightAssessment assess(Bird bird) {
        Instant now = clock.instant();
        List<WeightReading> history = weights
                .findByBirdIdAndMeasuredAtAfterOrderByMeasuredAtAsc(bird.getId(),
                        now.minus(WeightTrendAnalyzer.BASELINE_WINDOW))
                .stream().map(WeightEntry::toReading).toList();
        return analyzer.assess(history, now, bird.getTargetMinGrams(), bird.getTargetMaxGrams());
    }

    @Transactional(readOnly = true)
    public Page<WeightEntry> history(Bird bird, Pageable pageable) {
        return weights.findByBirdIdOrderByMeasuredAtDesc(bird.getId(), pageable);
    }
}
