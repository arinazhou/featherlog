package com.arinazhou.featherlog.weight;

import static org.assertj.core.api.Assertions.assertThat;

import com.arinazhou.featherlog.alert.AlertType;
import com.arinazhou.featherlog.alert.Severity;
import com.arinazhou.featherlog.weight.WeightAssessment.Finding;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeightTrendAnalyzerTest {

    private static final Instant NOW = Instant.parse("2026-06-30T08:00:00Z");
    private final WeightTrendAnalyzer analyzer = new WeightTrendAnalyzer();

    private static WeightReading daysAgo(double days, double grams) {
        return new WeightReading(grams, NOW.minus(Duration.ofMinutes((long) (days * 24 * 60))));
    }

    /** Stable ~35 g baseline from 10-20 days ago. */
    private static List<WeightReading> stableBaseline() {
        List<WeightReading> list = new ArrayList<>();
        double[] grams = {35.0, 35.4, 34.8, 35.2, 35.1};
        for (int i = 0; i < grams.length; i++) {
            list.add(daysAgo(20 - i * 2, grams[i]));
        }
        return list;
    }

    private WeightAssessment assess(List<WeightReading> history) {
        return analyzer.assess(history, NOW, 30.0, 40.0);
    }

    @Test
    void noReadingsIsInsufficientData() {
        WeightAssessment a = assess(List.of());
        assertThat(a.status()).isEqualTo(HealthStatus.INSUFFICIENT_DATA);
        assertThat(a.findings()).isEmpty();
    }

    @Test
    void tooFewBaselineReadingsIsInsufficientData() {
        WeightAssessment a = assess(List.of(daysAgo(10, 35), daysAgo(1, 35)));
        assertThat(a.status()).isEqualTo(HealthStatus.INSUFFICIENT_DATA);
        assertThat(a.baseline()).isNull();
        assertThat(a.latestGrams()).isEqualTo(35.0);
    }

    @Test
    void stableWeightIsHealthy() {
        List<WeightReading> h = stableBaseline();
        h.add(daysAgo(3, 35.0));
        h.add(daysAgo(1, 35.3));
        WeightAssessment a = assess(h);
        assertThat(a.status()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(a.baseline()).isEqualTo(35.1);
        assertThat(a.findings()).isEmpty();
    }

    @Test
    void moderateDropIsWarning() {
        List<WeightReading> h = stableBaseline();
        h.add(daysAgo(4, 33.2));
        h.add(daysAgo(2, 33.0));
        WeightAssessment a = assess(h);
        assertThat(a.status()).isEqualTo(HealthStatus.WATCH);
        assertThat(a.findings()).extracting(Finding::type, Finding::severity)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(AlertType.WEIGHT_DROP, Severity.WARNING));
    }

    @Test
    void largeDropIsCritical() {
        List<WeightReading> h = stableBaseline();
        h.add(daysAgo(5, 32.0));
        h.add(daysAgo(3, 31.2));
        h.add(daysAgo(1, 30.8));
        WeightAssessment a = assess(h);
        assertThat(a.status()).isEqualTo(HealthStatus.CONCERN);
        assertThat(a.changePercent()).isLessThanOrEqualTo(-10.0);
        assertThat(a.findings()).anySatisfy(f -> {
            assertThat(f.type()).isEqualTo(AlertType.WEIGHT_DROP);
            assertThat(f.severity()).isEqualTo(Severity.CRITICAL);
        });
    }

    @Test
    void baselineUsesMedianSoOneOutlierDoesNotSkewIt() {
        List<WeightReading> h = stableBaseline();
        h.add(daysAgo(9, 45.0)); // weighed right after a big meal
        h.add(daysAgo(8, 35.0));
        h.add(daysAgo(2, 34.0));
        WeightAssessment a = assess(h);
        // A mean baseline would be ~36.5 g and turn 34.0 g into a false "weight drop" warning.
        assertThat(a.baseline()).isEqualTo(35.1);
        assertThat(a.status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void suddenChangeBetweenConsecutiveReadingsIsFlagged() {
        List<WeightReading> h = stableBaseline();
        h.add(daysAgo(2, 35.0));
        h.add(daysAgo(1, 32.4));
        WeightAssessment a = assess(h);
        assertThat(a.findings()).extracting(Finding::type).contains(AlertType.RAPID_WEIGHT_CHANGE);
    }

    @Test
    void sameChangeSpreadOverAWeekIsNotRapid() {
        List<WeightReading> h = new ArrayList<>(List.of(daysAgo(8, 35.0), daysAgo(1, 32.4)));
        WeightAssessment a = assess(h);
        assertThat(a.findings()).extracting(Finding::type).doesNotContain(AlertType.RAPID_WEIGHT_CHANGE);
    }

    @Test
    void outOfTargetRangeIsFlagged() {
        WeightAssessment a = assess(List.of(daysAgo(1, 28.5)));
        assertThat(a.findings()).extracting(Finding::type).containsExactly(AlertType.OUT_OF_RANGE);
        assertThat(a.status()).isEqualTo(HealthStatus.WATCH);
    }

    @Test
    void futureReadingsAreIgnored() {
        List<WeightReading> h = stableBaseline();
        h.add(new WeightReading(20.0, NOW.plus(Duration.ofDays(1))));
        WeightAssessment a = assess(h);
        assertThat(a.latestGrams()).isEqualTo(35.1);
    }
}
