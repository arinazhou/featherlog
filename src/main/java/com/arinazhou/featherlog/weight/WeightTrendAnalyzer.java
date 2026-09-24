package com.arinazhou.featherlog.weight;

import com.arinazhou.featherlog.alert.AlertType;
import com.arinazhou.featherlog.alert.Severity;
import com.arinazhou.featherlog.weight.WeightAssessment.Finding;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Detects worrying weight trends.
 *
 * <p>Small birds hide illness well, and a steady weight loss is often the first visible sign. A 35 g
 * budgie losing 3-4 g is already a 10% drop, so the analyzer compares the last week against the bird's
 * own earlier baseline instead of relying on a fixed range alone.
 *
 * <ul>
 *   <li><b>Baseline</b>: median of readings between 30 and 7 days ago (median resists one-off
 *       readings taken right after a meal). Requires at least {@value #MIN_BASELINE_READINGS}.</li>
 *   <li><b>Recent</b>: mean of readings from the last 7 days.</li>
 *   <li><b>WEIGHT_DROP</b>: recent is 5% (WARNING) or 10% (CRITICAL) below baseline.</li>
 *   <li><b>RAPID_WEIGHT_CHANGE</b>: consecutive readings less than 72h apart differ by 7% or more.</li>
 *   <li><b>OUT_OF_RANGE</b>: latest reading is outside the bird's target range.</li>
 * </ul>
 */
@Component
public class WeightTrendAnalyzer {

    static final Duration RECENT_WINDOW = Duration.ofDays(7);
    static final Duration BASELINE_WINDOW = Duration.ofDays(30);
    static final Duration RAPID_CHANGE_WINDOW = Duration.ofHours(72);
    static final int MIN_BASELINE_READINGS = 3;
    static final double DROP_WARNING_PCT = 5.0;
    static final double DROP_CRITICAL_PCT = 10.0;
    static final double RAPID_CHANGE_PCT = 7.0;

    public WeightAssessment assess(List<WeightReading> history, Instant now, double minGrams, double maxGrams) {
        List<WeightReading> sorted = history.stream()
                .filter(r -> !r.measuredAt().isAfter(now))
                .sorted(Comparator.comparing(WeightReading::measuredAt))
                .toList();
        if (sorted.isEmpty()) {
            return new WeightAssessment(HealthStatus.INSUFFICIENT_DATA, null, null, null, null, List.of());
        }

        Instant recentStart = now.minus(RECENT_WINDOW);
        Instant baselineStart = now.minus(BASELINE_WINDOW);
        List<Double> recent = sorted.stream()
                .filter(r -> r.measuredAt().isAfter(recentStart))
                .map(WeightReading::grams).toList();
        List<Double> baselineReadings = sorted.stream()
                .filter(r -> r.measuredAt().isAfter(baselineStart) && !r.measuredAt().isAfter(recentStart))
                .map(WeightReading::grams).toList();

        WeightReading latest = sorted.get(sorted.size() - 1);
        Double recentAvg = recent.isEmpty() ? null : round(mean(recent));
        Double baseline = baselineReadings.size() >= MIN_BASELINE_READINGS ? round(median(baselineReadings)) : null;
        Double changePct = recentAvg != null && baseline != null
                ? round((recentAvg - baseline) / baseline * 100.0) : null;

        List<Finding> findings = new ArrayList<>();
        if (changePct != null && -changePct >= DROP_WARNING_PCT) {
            Severity severity = -changePct >= DROP_CRITICAL_PCT ? Severity.CRITICAL : Severity.WARNING;
            findings.add(new Finding(AlertType.WEIGHT_DROP, severity, String.format(
                    "7-day average %.1f g is %.1f%% below baseline %.1f g", recentAvg, -changePct, baseline)));
        }

        if (sorted.size() >= 2) {
            WeightReading previous = sorted.get(sorted.size() - 2);
            Duration gap = Duration.between(previous.measuredAt(), latest.measuredAt());
            double pct = (latest.grams() - previous.grams()) / previous.grams() * 100.0;
            if (gap.compareTo(RAPID_CHANGE_WINDOW) <= 0 && Math.abs(pct) >= RAPID_CHANGE_PCT) {
                findings.add(new Finding(AlertType.RAPID_WEIGHT_CHANGE, Severity.WARNING, String.format(
                        "Weight changed %+.1f%% (%.1f g -> %.1f g) in %d hours",
                        pct, previous.grams(), latest.grams(), gap.toHours())));
            }
        }

        if (latest.grams() < minGrams || latest.grams() > maxGrams) {
            findings.add(new Finding(AlertType.OUT_OF_RANGE, Severity.WARNING, String.format(
                    "Latest weight %.1f g is outside target range %.1f-%.1f g", latest.grams(), minGrams, maxGrams)));
        }

        return new WeightAssessment(statusFor(findings, baseline), latest.grams(), recentAvg, baseline, changePct,
                List.copyOf(findings));
    }

    private static HealthStatus statusFor(List<Finding> findings, Double baseline) {
        if (findings.stream().anyMatch(f -> f.severity() == Severity.CRITICAL)) {
            return HealthStatus.CONCERN;
        }
        if (!findings.isEmpty()) {
            return HealthStatus.WATCH;
        }
        return baseline == null ? HealthStatus.INSUFFICIENT_DATA : HealthStatus.HEALTHY;
    }

    private static double mean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    }

    private static double median(List<Double> values) {
        List<Double> s = values.stream().sorted().toList();
        int mid = s.size() / 2;
        return s.size() % 2 == 1 ? s.get(mid) : (s.get(mid - 1) + s.get(mid)) / 2.0;
    }

    private static double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
