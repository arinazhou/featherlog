package com.arinazhou.featherlog.weight;

import java.time.Instant;

/** A single weigh-in, decoupled from JPA so the trend analysis can be unit tested in isolation. */
public record WeightReading(double grams, Instant measuredAt) {
}
