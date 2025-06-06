package com.example.Diallock_AI.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.Diallock_AI.model.FollowUpPlan;

public class FollowUpScheduler {

    public static List<LocalDate> getFollowUpDates(LocalDate startDate, FollowUpPlan plan) {
        List<Integer> offsets = plan.getDayOffsets();
        List<LocalDate> scheduledDates = new ArrayList<>();

        for (Integer offset : offsets) {
            LocalDate tentativeDate = startDate.plusDays(offset);
            // Adjust to next working day if weekend
            while (tentativeDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                   tentativeDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                tentativeDate = tentativeDate.plusDays(1);
            }
            scheduledDates.add(tentativeDate);
        }

        return scheduledDates;
    }
}
