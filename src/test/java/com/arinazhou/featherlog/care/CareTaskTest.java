package com.arinazhou.featherlog.care;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CareTaskTest {

    private static final Instant T0 = Instant.parse("2026-06-01T09:00:00Z");

    @Test
    void firstDueDateIsOneIntervalAfterCreation() {
        CareTask task = new CareTask(null, CareTaskType.CAGE_CLEAN, "Clean", 7, T0);
        assertThat(task.getNextDueAt()).isEqualTo(T0.plus(Duration.ofDays(7)));
        assertThat(task.isOverdue(T0.plus(Duration.ofDays(6)))).isFalse();
        assertThat(task.isOverdue(T0.plus(Duration.ofDays(8)))).isTrue();
    }

    @Test
    void completingResetsTheScheduleFromCompletionTime() {
        CareTask task = new CareTask(null, CareTaskType.FRESH_WATER, "Water", 1, T0);
        Instant late = T0.plus(Duration.ofDays(3));
        task.complete(late);
        assertThat(task.getLastCompletedAt()).isEqualTo(late);
        assertThat(task.getNextDueAt()).isEqualTo(late.plus(Duration.ofDays(1)));
        assertThat(task.isOverdue(late.plus(Duration.ofHours(12)))).isFalse();
    }

    @Test
    void inactiveTasksAreNeverOverdue() {
        CareTask task = new CareTask(null, CareTaskType.FRESH_FOOD, "Food", 1, T0);
        task.deactivate();
        assertThat(task.isOverdue(T0.plus(Duration.ofDays(30)))).isFalse();
    }

    @Test
    void rejectsNonPositiveInterval() {
        assertThatThrownBy(() -> new CareTask(null, CareTaskType.CUSTOM, "x", 0, T0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
