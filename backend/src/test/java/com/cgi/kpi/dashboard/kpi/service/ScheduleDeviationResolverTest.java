package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ScheduleDeviationResolverTest {

    @Test
    void derivesDeviationFromPlannedAndPredictedDates() {
        Integer deviation = ScheduleDeviationResolver.resolve(
                LocalDate.of(2026, 6, 30),
                LocalDate.of(2026, 7, 15),
                0);

        assertEquals(15, deviation);
    }

    @Test
    void fallsBackToStoredValueWhenPredictionMissing() {
        Integer deviation = ScheduleDeviationResolver.resolve(
                LocalDate.of(2026, 6, 30),
                null,
                12);

        assertEquals(12, deviation);
    }

    @Test
    void returnsNullWhenNoDatesAvailable() {
        assertNull(ScheduleDeviationResolver.resolve(null, null, null));
    }
}
