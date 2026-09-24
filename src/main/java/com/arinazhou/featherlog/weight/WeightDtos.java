package com.arinazhou.featherlog.weight;

import com.arinazhou.featherlog.alert.AlertResponse;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class WeightDtos {

    private WeightDtos() {
    }

    /** measuredAt defaults to now, so a quick morning weigh-in only needs grams. */
    public record WeightRequest(
            @NotNull @DecimalMin("5.0") @DecimalMax("500.0") Double grams,
            @PastOrPresent Instant measuredAt,
            @Size(max = 500) String note) {
    }

    public record WeightResponse(Long id, double grams, Instant measuredAt, String note) {

        static WeightResponse from(WeightEntry e) {
            return new WeightResponse(e.getId(), e.getGrams(), e.getMeasuredAt(), e.getNote());
        }
    }

    /** Returned after logging a weight: the saved entry, the fresh assessment, and any alerts it opened. */
    public record WeightLogResult(WeightResponse entry, WeightAssessment assessment, List<AlertResponse> newAlerts) {
    }
}
