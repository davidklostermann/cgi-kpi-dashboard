package com.cgi.kpi.dashboard.kpi.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Consistent schedule deviation from plan vs. forecast dates (Epic 5 review). */
public final class ScheduleDeviationResolver {

    private ScheduleDeviationResolver() {
    }

    public static Integer resolve(LocalDate plannedEndDate, LocalDate predictedEndDate, Integer storedDays) {
        if (plannedEndDate != null && predictedEndDate != null) {
            return (int) ChronoUnit.DAYS.between(plannedEndDate, predictedEndDate);
        }
        return storedDays;
    }
}
