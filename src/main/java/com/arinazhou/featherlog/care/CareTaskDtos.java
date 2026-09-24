package com.arinazhou.featherlog.care;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class CareTaskDtos {

    private CareTaskDtos() {
    }

    /** title and intervalDays fall back to the type's defaults when omitted. */
    public record CareTaskRequest(
            @NotNull CareTaskType type,
            @Size(max = 100) String title,
            @Min(1) @Max(730) Integer intervalDays) {
    }

    public record CareTaskResponse(
            Long id,
            CareTaskType type,
            String title,
            int intervalDays,
            Instant lastCompletedAt,
            Instant nextDueAt,
            boolean overdue) {

        static CareTaskResponse from(CareTask t, Instant now) {
            return new CareTaskResponse(t.getId(), t.getType(), t.getTitle(), t.getIntervalDays(),
                    t.getLastCompletedAt(), t.getNextDueAt(), t.isOverdue(now));
        }
    }
}
