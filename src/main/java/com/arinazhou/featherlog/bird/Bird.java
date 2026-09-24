package com.arinazhou.featherlog.bird;

import com.arinazhou.featherlog.user.AppUser;
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
import java.time.LocalDate;

@Entity
@Table(name = "bird")
public class Bird {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String species;

    @Column(name = "color_mutation")
    private String colorMutation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Column(name = "hatch_date")
    private LocalDate hatchDate;

    @Column(name = "target_min_grams", nullable = false)
    private double targetMinGrams;

    @Column(name = "target_max_grams", nullable = false)
    private double targetMaxGrams;

    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Bird() {
    }

    public Bird(AppUser owner, Instant createdAt) {
        this.owner = owner;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public AppUser getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getColorMutation() {
        return colorMutation;
    }

    public void setColorMutation(String colorMutation) {
        this.colorMutation = colorMutation;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public LocalDate getHatchDate() {
        return hatchDate;
    }

    public void setHatchDate(LocalDate hatchDate) {
        this.hatchDate = hatchDate;
    }

    public double getTargetMinGrams() {
        return targetMinGrams;
    }

    public double getTargetMaxGrams() {
        return targetMaxGrams;
    }

    public void setTargetRange(double min, double max) {
        if (min <= 0 || max <= min) {
            throw new IllegalArgumentException("Target weight range must satisfy 0 < min < max");
        }
        this.targetMinGrams = min;
        this.targetMaxGrams = max;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
