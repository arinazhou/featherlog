package com.arinazhou.featherlog.bird;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public final class BirdDtos {

    private BirdDtos() {
    }

    /** Create/replace payload. Species and weight range default to typical pet budgie values. */
    public record BirdRequest(
            @NotBlank @Size(max = 60) String name,
            @Size(max = 60) String species,
            @Size(max = 60) String colorMutation,
            Sex sex,
            @PastOrPresent LocalDate hatchDate,
            @DecimalMin("5.0") @DecimalMax("500.0") Double targetMinGrams,
            @DecimalMin("5.0") @DecimalMax("500.0") Double targetMaxGrams,
            @Size(max = 1000) String notes) {
    }

    public record BirdResponse(
            Long id,
            String name,
            String species,
            String colorMutation,
            Sex sex,
            LocalDate hatchDate,
            double targetMinGrams,
            double targetMaxGrams,
            String notes) {

        static BirdResponse from(Bird b) {
            return new BirdResponse(b.getId(), b.getName(), b.getSpecies(), b.getColorMutation(), b.getSex(),
                    b.getHatchDate(), b.getTargetMinGrams(), b.getTargetMaxGrams(), b.getNotes());
        }
    }
}
