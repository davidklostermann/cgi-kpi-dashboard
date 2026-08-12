package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PortfolioStatusLabelsTest {

    @Test
    void mapsPortfolioStatusCodesToGermanWords() {
        assertEquals("Auf Kurs", PortfolioStatusLabels.toGermanLabel("ON_TRACK"));
        assertEquals("Beobachten", PortfolioStatusLabels.toGermanLabel("AT_RISK"));
        assertEquals("Kritisch", PortfolioStatusLabels.toGermanLabel("CRITICAL"));
        assertEquals("Abgeschlossen", PortfolioStatusLabels.toGermanLabel("COMPLETED"));
    }
}
