# Input Reconciliation: Product Brief + KPI-Addendum

**Status:** vollständig abgebildet in `prd.md` (aktualisiert 2026-07-15)

## Brief → PRD Mapping

| Brief-Inhalt | PRD-Heimat | Lücken |
|--------------|------------|--------|
| Produkthypothese | §1 Vision | — |
| 10 Leitfragen | implizit in FR-1–6, FR-4, FR-20 | nicht als Liste wiederholt (bewusst) |
| KPI vs. KI-Trennung | §1, FR-9/10, §4.8–4.9 | — |
| Management-Einzelprojekt-Analyse | §1, FR-5, FR-20, FR-21 | — |
| MVP In/Out | §5, §6 | — |
| Nutzer FK/PL | §2 | — |
| Success Criteria Pilot | §7 SM-1–6 | Messmethoden `[OFFEN]` |
| Offene Punkte Brief | §8 (erweitert) | — |
| Tech-Stack | PRD-`addendum.md` | bewusst aus PRD-Haupttext ausgelagert |

## Brief-Addendum (KPI-Inventar) → PRD

| Addendum-Bereich | PRD-Heimat |
|------------------|------------|
| 5 KPI-Bereiche + erweiterte Metriken | FR-9 verweist auf Brief-Addendum; Formeln `[OFFEN]` |
| §5 Management Insights | FR-20 |
| §6 Berichtsstandsvergleich | FR-21 |
| §7 Gemini-Ausgabetypen | FR-4, FR-11, FR-12, FR-16 |
| Keine neuen KPIs erfunden | bestätigt |

## PRD-Erweiterungen gegenüber Brief (bewusst)

- Portfolio-Trendanalyse als integrierter KI-Text (FR-4)
- Projekt-KI-Q&A vollständig Gemini-basiert (FR-16–18)
- Deterministische Management Insights (FR-20)
- Berichtsstandsvergleich mit MVP-Snapshot-Modell `[ASSUMPTION]` (FR-21)
- User Journeys UJ-1/UJ-2 mit Sabine/Markus
- FR-1–21 mit testbaren Consequences

## Qualitative Ideen aus Brief (nicht in FR-Struktur verloren)

- „Nice-to-have", kein Pflichtsystem → §1, Non-Goals implizit
- Fehlschlag = Zahlenflut → SM-C2, FR-10
- Greenfield → FR-19
- Kein PM-/Aufgaben-/Ressourcenplanungssystem → FR-5/6 Scope-Hinweise

**Fazit:** Keine substantiellen Lücken. Offene Punkte bewusst in §8 und als `[OFFEN]`/`[ASSUMPTION]` markiert.
