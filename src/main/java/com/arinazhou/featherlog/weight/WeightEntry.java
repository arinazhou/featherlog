package com.arinazhou.featherlog.weight;

import com.arinazhou.featherlog.bird.Bird;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "weight_entry")
public class WeightEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bird_id")
    private Bird bird;

    @Column(nullable = false)
    private double grams;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    private String note;

    protected WeightEntry() {
    }

    public WeightEntry(Bird bird, double grams, Instant measuredAt, String note) {
        this.bird = bird;
        this.grams = grams;
        this.measuredAt = measuredAt;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public Bird getBird() {
        return bird;
    }

    public double getGrams() {
        return grams;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }

    public String getNote() {
        return note;
    }

    public WeightReading toReading() {
        return new WeightReading(grams, measuredAt);
    }
}
