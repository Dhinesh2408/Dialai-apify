package com.example.Diallock_AI.model;

import java.util.List;

public enum FollowUpPlan {
    ACCELERATED_15(List.of(1, 2, 5, 9, 12, 15)),
    BALANCED_21(List.of(1, 2, 6, 10, 14, 18, 21)),
    EXTENDED_30(List.of(1, 3, 8, 13, 18, 23, 30));

    private final List<Integer> dayOffsets;

    FollowUpPlan(List<Integer> dayOffsets) {
        this.dayOffsets = dayOffsets;
    }

    public List<Integer> getDayOffsets() {
        return dayOffsets;
    }
}
