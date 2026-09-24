package com.arinazhou.featherlog.weight;

import com.arinazhou.featherlog.alert.AlertType;
import com.arinazhou.featherlog.alert.Severity;
import java.util.List;

/**
 * Result of analysing a bird's recent weigh-ins.
 *
 * @param latestGrams      most recent reading, or null when there is none
 * @param recentAverage    mean of readings in the recent window, or null
 * @param baseline         median of readings in the baseline window, or null if too few
 * @param changePercent    (recentAverage - baseline) / baseline * 100, or null
 */
public record WeightAssessment(
        HealthStatus status,
        Double latestGrams,
        Double recentAverage,
        Double baseline,
        Double changePercent,
        List<Finding> findings) {

    public record Finding(AlertType type, Severity severity, String message) {
    }
}
