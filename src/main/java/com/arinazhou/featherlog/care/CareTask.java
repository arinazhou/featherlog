package com.arinazhou.featherlog.care;

import com.arinazhou.featherlog.bird.Bird;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "care_task")
public class CareTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bird_id")
    private Bird bird;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareTaskType type;

    @Column(nullable = false)
    private String title;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays;

    @Column(name = "last_completed_at")
    private Instant lastCompletedAt;

    /** Denormalized so the reminder job can find overdue tasks with one indexed query. */
    @Column(name = "next_due_at", nullable = false)
    private Instant nextDueAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CareTask() {
    }

    public CareTask(Bird bird, CareTaskType type, String title, int intervalDays, Instant now) {
        if (intervalDays < 1) {
            throw new IllegalArgumentException("intervalDays must be at least 1");
        }
        this.bird = bird;
        this.type = type;
        this.title = title;
        this.intervalDays = intervalDays;
        this.createdAt = now;
        this.nextDueAt = now.plus(Duration.ofDays(intervalDays));
    }

    public void complete(Instant at) {
        this.lastCompletedAt = at;
        this.nextDueAt = at.plus(Duration.ofDays(intervalDays));
    }

    public boolean isOverdue(Instant now) {
        return active && nextDueAt.isBefore(now);
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() {
        return id;
    }

    public Bird getBird() {
        return bird;
    }

    public CareTaskType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public Instant getLastCompletedAt() {
        return lastCompletedAt;
    }

    public Instant getNextDueAt() {
        return nextDueAt;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
