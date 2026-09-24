package com.arinazhou.featherlog.alert;

import com.arinazhou.featherlog.bird.Bird;
import com.arinazhou.featherlog.care.CareTask;
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
import java.time.Instant;

@Entity
@Table(name = "health_alert")
public class HealthAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bird_id")
    private Bird bird;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_task_id")
    private CareTask careTask;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected HealthAlert() {
    }

    public HealthAlert(Bird bird, CareTask careTask, AlertType type, Severity severity, String message,
                       Instant createdAt) {
        this.bird = bird;
        this.careTask = careTask;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.createdAt = createdAt;
    }

    public void resolve(Instant at) {
        if (resolvedAt == null) {
            resolvedAt = at;
        }
    }

    public boolean isOpen() {
        return resolvedAt == null;
    }

    public Long getId() {
        return id;
    }

    public Bird getBird() {
        return bird;
    }

    public CareTask getCareTask() {
        return careTask;
    }

    public AlertType getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
