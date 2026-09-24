package com.arinazhou.featherlog.alert;

public enum AlertType {
    /** Recent average weight is well below the bird's own baseline. */
    WEIGHT_DROP,
    /** Large change between two consecutive weigh-ins. */
    RAPID_WEIGHT_CHANGE,
    /** Latest weight is outside the bird's configured healthy range. */
    OUT_OF_RANGE,
    /** A recurring care task is past its due time. */
    CARE_OVERDUE
}
