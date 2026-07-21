import { useMemo, useState } from 'react';
import Spinner from '../../components/Spinner';
import EmptyState from '../../components/EmptyState';
import { useTimeEntries } from './hooks';
import { addDays, formatWeekday, startOfWeek, toIsoDate } from '../../utils/date';
import styles from './TimeEntriesPage.module.css';

const TYPE_LABELS: Record<string, string> = {
  OFFICE: 'Escritório',
  REMOTE: 'Remoto',
  ON_SITE: 'Cliente',
  OVERTIME: 'Horas extra',
};

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Rascunho',
  SUBMITTED: 'Submetido',
  APPROVED: 'Aprovado',
  REJECTED: 'Rejeitado',
};

function formatDuration(minutes: number) {
  const sign = minutes < 0 ? '-' : '';
  const abs = Math.abs(minutes);
  const hours = Math.floor(abs / 60);
  const mins = abs % 60;
  return `${sign}${hours}h${mins.toString().padStart(2, '0')}`;
}

function TimeEntriesPage() {
  const [reference, setReference] = useState(() => new Date());
  const weekStart = useMemo(() => startOfWeek(reference), [reference]);
  const weekEnd = useMemo(() => addDays(weekStart, 6), [weekStart]);
  const from = toIsoDate(weekStart);
  const to = toIsoDate(weekEnd);

  const { data: entries, isLoading } = useTimeEntries(from, to);

  const total = (entries ?? []).reduce((sum, entry) => sum + entry.durationMinutes, 0);

  return (
    <div>
      <div className={styles.header}>
        <div className={styles.weekNav}>
          <button
            type="button"
            className={styles.navButton}
            onClick={() => setReference((current) => addDays(current, -7))}
          >
            ← semana anterior
          </button>
          <span className={styles.weekLabel}>
            {weekStart.toLocaleDateString('pt-PT')} — {weekEnd.toLocaleDateString('pt-PT')}
          </span>
          <button
            type="button"
            className={styles.navButton}
            onClick={() => setReference((current) => addDays(current, 7))}
          >
            semana seguinte →
          </button>
        </div>
        <div className={styles.summary}>
          <span>Total da semana</span>
          <span className={styles.summaryValue}>{formatDuration(total)}</span>
        </div>
      </div>

      {isLoading && <Spinner />}

      {!isLoading && (entries ?? []).length === 0 && (
        <EmptyState message="Sem registos nesta semana." />
      )}

      {!isLoading && (entries ?? []).length > 0 && (
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Dia</th>
              <th>Início</th>
              <th>Fim</th>
              <th>Pausa</th>
              <th>Duração</th>
              <th>Tipo</th>
              <th>Notas</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {(entries ?? [])
              .slice()
              .sort((a, b) => a.workDate.localeCompare(b.workDate))
              .map((entry) => (
                <tr key={entry.id}>
                  <td className={styles.weekdayCell}>
                    {formatWeekday(new Date(`${entry.workDate}T00:00:00`))} {entry.workDate.slice(8, 10)}/
                    {entry.workDate.slice(5, 7)}
                  </td>
                  <td>{entry.startTime.slice(0, 5)}</td>
                  <td>{entry.endTime.slice(0, 5)}</td>
                  <td>{entry.breakMinutes} min</td>
                  <td>{formatDuration(entry.durationMinutes)}</td>
                  <td>{TYPE_LABELS[entry.type] ?? entry.type}</td>
                  <td className={styles.notesCell}>{entry.notes}</td>
                  <td>{STATUS_LABELS[entry.status] ?? entry.status}</td>
                </tr>
              ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default TimeEntriesPage;