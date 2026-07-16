import { Component, computed, input } from '@angular/core';

import { PortfolioTimelineProject } from '../models/portfolio-timeline.model';

/** Keep left name column and timeline scroll vertically aligned. */
export function syncScrollTops(source: HTMLElement, target: HTMLElement): void {
  if (target.scrollTop !== source.scrollTop) {
    target.scrollTop = source.scrollTop;
  }
}

export interface GanttAxisMonth {
  key: string;
  monthLabel: string;
  yearLabel: string;
}

export interface GanttAxisYear {
  yearLabel: string;
  monthCount: number;
  widthRem: number;
}

export interface GanttHandoverMarker {
  kind: 'current' | 'plan';
  label: string;
  detailLabel: string | null;
  offsetPercent: number;
  delayLabel: string | null;
  tooltip: string;
  delayed: boolean;
}

export interface GanttRowLayout {
  project: PortfolioTimelineProject;
  barLeftPercent: number;
  barWidthPercent: number;
  overrunLeftPercent: number | null;
  overrunWidthPercent: number | null;
  phaseSegments: {
    leftPercent: number;
    widthPercent: number;
    name: string;
    phaseType: string;
  }[];
  handoverMarkers: GanttHandoverMarker[];
}

const MS_PER_DAY = 86_400_000;
const MONTH_COL_REM = 5.5;

/** Portfolio Gantt timeline — CGI-konforme HTML/CSS presentation (FR-3 / Story 5.2). */
@Component({
  selector: 'app-gantt-timeline',
  imports: [],
  templateUrl: './gantt-timeline.component.html',
  styleUrl: './gantt-timeline.component.scss',
})
export class GanttTimelineComponent {
  readonly projects = input.required<PortfolioTimelineProject[]>();

  private readonly today = startOfDay(new Date());

  readonly range = computed(() => {
    const projects = this.projects();
    if (projects.length === 0) {
      const start = firstDayOfMonth(this.today);
      return { start, end: lastDayOfMonth(this.today) };
    }

    let startMs = Number.POSITIVE_INFINITY;
    let endMs = Number.NEGATIVE_INFINITY;

    for (const project of projects) {
      startMs = Math.min(startMs, parseDate(project.startDate));
      endMs = Math.max(
        endMs,
        parseDate(project.plannedEndDate),
        project.forecastEndDate ? parseDate(project.forecastEndDate) : 0,
        project.actualEndDate ? parseDate(project.actualEndDate) : 0,
      );
      for (const phase of project.phases) {
        startMs = Math.min(startMs, parseDate(phase.startDate));
        endMs = Math.max(endMs, parseDate(phase.endDate));
      }
      for (const milestone of project.milestones) {
        endMs = Math.max(
          endMs,
          parseDate(milestone.dueDate),
          milestone.completedDate ? parseDate(milestone.completedDate) : 0,
        );
      }
    }

    const earliest = new Date(startMs);
    const latest = new Date(endMs);
    return {
      start: firstDayOfMonth(shiftMonth(earliest, -1)),
      end: lastDayOfMonth(shiftMonth(latest, 1)),
    };
  });

  readonly axisMonths = computed<GanttAxisMonth[]>(() => {
    const { start, end } = this.range();
    const months: GanttAxisMonth[] = [];
    const cursor = firstDayOfMonth(start);
    const endMonth = firstDayOfMonth(end);

    while (cursor <= endMonth) {
      const key = `${cursor.getFullYear()}-${String(cursor.getMonth() + 1).padStart(2, '0')}`;
      months.push({
        key,
        monthLabel: cursor.toLocaleDateString('de-DE', { month: 'short' }),
        yearLabel: String(cursor.getFullYear()),
      });
      cursor.setMonth(cursor.getMonth() + 1);
    }

    return months;
  });

  readonly axisYears = computed<GanttAxisYear[]>(() => {
    const groups: GanttAxisYear[] = [];
    for (const month of this.axisMonths()) {
      const current = groups[groups.length - 1];
      if (current?.yearLabel === month.yearLabel) {
        current.monthCount += 1;
        current.widthRem = current.monthCount * MONTH_COL_REM;
      } else {
        groups.push({
          yearLabel: month.yearLabel,
          monthCount: 1,
          widthRem: MONTH_COL_REM,
        });
      }
    }
    return groups;
  });

  readonly trackWidthRem = computed(
    () => this.axisMonths().length * MONTH_COL_REM,
  );

  readonly todayVisible = computed(() => {
    const { start, end } = this.range();
    return this.today >= start && this.today <= end;
  });

  readonly todayPercent = computed(() => this.dateToTrackPercent(this.today));

  readonly rows = computed<GanttRowLayout[]>(() =>
    this.projects().map((project) => this.toRowLayout(project)),
  );

  readonly screenReaderSummary = computed(() =>
    this.projects()
      .map((project) => {
        const currentEnd = this.currentEndDate(project);
        const delayDays = daysBetween(
          parseIso(project.plannedEndDate),
          parseIso(currentEnd),
        );
        if (delayDays > 0) {
          return (
            `${project.name}: ${project.statusLabel}, ` +
            `geplante Kundenübergabe ${this.formatDate(project.plannedEndDate)}, ` +
            `aktuelle Prognose ${this.formatDate(currentEnd)}, ` +
            `${delayDays} Tage Verzug`
          );
        }
        return (
          `${project.name}: ${project.statusLabel}, ` +
          `Kundenübergabe ${this.formatDate(currentEnd)}`
        );
      })
      .join('. '),
  );

  onTimelineScroll(event: Event): void {
    const source = event.target as HTMLElement;
    const names = source
      .closest('.gantt-timeline__frame')
      ?.querySelector('.gantt-timeline__names') as HTMLElement | null;
    if (names) {
      syncScrollTops(source, names);
    }
  }

  onNamesScroll(event: Event): void {
    const source = event.target as HTMLElement;
    const timeline = source
      .closest('.gantt-timeline__frame')
      ?.querySelector('.gantt-timeline__scroll') as HTMLElement | null;
    if (timeline) {
      syncScrollTops(source, timeline);
    }
  }

  private toRowLayout(project: PortfolioTimelineProject): GanttRowLayout {
    const currentEndDate = this.currentEndDate(project);
    const barLeftPercent = this.dateToTrackPercent(parseIso(project.startDate));
    const currentEndPercent = this.dateToTrackPercent(parseIso(currentEndDate));
    const barWidthPercent = Math.max(0.35, currentEndPercent - barLeftPercent);
    const plannedEndPercent = this.dateToTrackPercent(
      parseIso(project.plannedEndDate),
    );
    const delayDays = daysBetween(
      parseIso(project.plannedEndDate),
      parseIso(currentEndDate),
    );
    const delayed = delayDays > 0;

    const overrunLeftPercent = delayed ? plannedEndPercent : null;
    const overrunWidthPercent = delayed
      ? Math.max(0.35, currentEndPercent - plannedEndPercent)
      : null;

    const handoverMarkers: GanttHandoverMarker[] = [];
    if (delayed) {
      handoverMarkers.push({
        kind: 'plan',
        label: 'Plan',
        detailLabel: this.formatDate(project.plannedEndDate),
        offsetPercent: plannedEndPercent,
        delayLabel: null,
        tooltip: `Geplante Kundenübergabe: ${this.formatDate(project.plannedEndDate)}`,
        delayed: true,
      });
    }

    handoverMarkers.push({
      kind: 'current',
      label: 'Kundenübergabe',
      detailLabel: delayed
        ? `Prognose ${this.formatDate(currentEndDate)}`
        : this.formatDate(currentEndDate),
      offsetPercent: currentEndPercent,
      delayLabel: delayed ? `+${delayDays} Tage` : null,
      tooltip: delayed
        ? `Kundenübergabe: Prognose ${this.formatDate(currentEndDate)}, ${delayDays} Tage nach Plan`
        : `Kundenübergabe: ${this.formatDate(currentEndDate)}`,
      delayed,
    });

    const phaseSegments = project.phases.map((phase) => {
      const phaseStart = this.dateToTrackPercent(parseIso(phase.startDate));
      const phaseEnd = this.dateToTrackPercent(parseIso(phase.endDate));
      return {
        name: phase.name,
        phaseType: phase.phaseType,
        leftPercent: clamp(
          ((phaseStart - barLeftPercent) / barWidthPercent) * 100,
        ),
        widthPercent: Math.max(
          0.5,
          ((phaseEnd - phaseStart) / barWidthPercent) * 100,
        ),
      };
    });

    return {
      project,
      barLeftPercent,
      barWidthPercent,
      overrunLeftPercent,
      overrunWidthPercent,
      phaseSegments,
      handoverMarkers,
    };
  }

  private currentEndDate(project: PortfolioTimelineProject): string {
    return (
      project.actualEndDate ?? project.forecastEndDate ?? project.plannedEndDate
    );
  }

  private dateToTrackPercent(date: Date): number {
    const months = this.axisMonths();
    if (months.length === 0) {
      return 0;
    }

    const { start } = this.range();
    const monthIndex =
      (date.getFullYear() - start.getFullYear()) * 12 +
      date.getMonth() -
      start.getMonth();
    const daysInMonth = new Date(
      date.getFullYear(),
      date.getMonth() + 1,
      0,
    ).getDate();
    const monthFraction = (date.getDate() - 1) / daysInMonth;
    return clamp(((monthIndex + monthFraction) / months.length) * 100);
  }

  private formatDate(iso: string): string {
    return parseIso(iso).toLocaleDateString('de-DE');
  }
}

function parseDate(iso: string): number {
  return parseIso(iso).getTime();
}

function parseIso(iso: string): Date {
  const [year, month, day] = iso.split('-').map(Number);
  return new Date(year, month - 1, day);
}

function startOfDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function firstDayOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

function lastDayOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0);
}

function shiftMonth(date: Date, delta: number): Date {
  return new Date(date.getFullYear(), date.getMonth() + delta, 1);
}

function daysBetween(start: Date, end: Date): number {
  return Math.round((end.getTime() - start.getTime()) / MS_PER_DAY);
}

function clamp(value: number): number {
  return Math.min(100, Math.max(0, value));
}
