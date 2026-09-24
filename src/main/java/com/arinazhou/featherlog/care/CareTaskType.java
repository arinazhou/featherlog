package com.arinazhou.featherlog.care;

/** Recurring care jobs for a pet budgie, with a sensible default interval for each. */
public enum CareTaskType {
    FRESH_WATER("Change drinking water", 1),
    FRESH_FOOD("Refresh seed/pellets and remove husks", 1),
    FRESH_GREENS("Offer fresh vegetables", 2),
    CAGE_CLEAN("Deep clean cage and perches", 7),
    CUTTLEBONE_CHECK("Check cuttlebone and mineral block", 14),
    NAIL_BEAK_CHECK("Check nails and beak length", 30),
    VET_CHECKUP("Avian vet wellness exam", 365),
    CUSTOM("Custom task", 7);

    private final String defaultTitle;
    private final int defaultIntervalDays;

    CareTaskType(String defaultTitle, int defaultIntervalDays) {
        this.defaultTitle = defaultTitle;
        this.defaultIntervalDays = defaultIntervalDays;
    }

    public String defaultTitle() {
        return defaultTitle;
    }

    public int defaultIntervalDays() {
        return defaultIntervalDays;
    }
}
