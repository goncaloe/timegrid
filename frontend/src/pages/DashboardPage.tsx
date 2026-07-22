import { useMemo } from 'react';
import Spinner from '../components/Spinner';
import EmptyState from '../components/EmptyState';
import { useTimeBankMe } from '../features/timebank/hooks';
import { useTimeEntries } from '../features/timeEntries/hooks';
import { useAbsences } from '../features/absences/hooks';
import { useHolidays } from '../features/holidays/hooks';
import { addDays, formatDate, startOfMonth, startOfWeek, endOfMonth, toIsoDate } from '../utils/date';
import { formatMinutes } from '../utils/formatMinutes';
import styles from './DashboardPage.module.css';

const ABSENCE_TYPE_LABELS: Record<string, string> = {
  VACATION: 'Férias',
  SICK: 'Doença',
  PERSONAL: 'Assuntos pessoais',
  TRAINING: 'Formação',
  OTHER: 'Outro',
};

function DashboardPage() {
  const today = useMemo(() => new Date(), []);
  const monthFrom = toIsoDate(startOfMonth(today));
  const monthTo = toIsoDate(endOfMonth(today));
  const weekStart = startOfWeek(today);
  const weekFrom = toIsoDate(weekStart);
  const weekTo = toIsoDate(addDays(weekStart, 6));
  const todayIso = toIsoDate(today);

  const { data: balance, isLoading: isLoadingBalance } = useTimeBankMe(monthFrom, monthTo);
  const { data: weekEntries, isLoading: isLoadingWeek } = useTimeEntries(weekFrom, weekTo);
  const { data: absences, isLoading: isLoadingAbsences } = useAbsences(today.getFullYear());
  const { data: holidays, isLoading: isLoadingHolidays } = useHolidays(today.getFullYear());

  const weekTotal = (weekEntries ?? []).reduce((sum, entry) => sum + entry.durationMinutes, 0);

  const upcomingAbsences = (absences ?? [])
    .filter((absence) => absence.startDate >= todayIso && (absence.status === 'SUBMITTED' || absence.status === 'APPROVED'))
    .sort((a, b) => a.startDate.localeCompare(b.startDate))
    .slice(0, 3);

  const nextHoliday = (holidays ?? [])
    .filter((holiday) => holiday.date >= todayIso)
    .sort((a, b) => a.date.localeCompare(b.date))[0];

  return (
    <div className={styles.grid}>
      <div className={styles.card}>
        <p className={styles.cardTitle}>Saldo do banco de horas (mês)</p>
        {isLoadingBalance && <Spinner />}
        {!isLoadingBalance && balance && (
          <p className={`${styles.cardValue} ${balance.balanceMinutes >= 0 ? styles.positive : styles.negative}`}>
            {formatMinutes(balance.balanceMinutes)}
          </p>
        )}
      </div>

      <div className={styles.card}>
        <p className={styles.cardTitle}>Horas esta semana</p>
        {isLoadingWeek && <Spinner />}
        {!isLoadingWeek && <p className={styles.cardValue}>{formatMinutes(weekTotal)}</p>}
      </div>

      <div className={styles.card}>
        <p className={styles.cardTitle}>Próximo feriado</p>
        {isLoadingHolidays && <Spinner />}
        {!isLoadingHolidays && !nextHoliday && <EmptyState message="Sem feriados agendados." />}
        {!isLoadingHolidays && nextHoliday && (
          <p className={styles.cardValue}>
            {nextHoliday.name}
            <br />
            <span className={styles.listItemMuted}>{formatDate(nextHoliday.date)}</span>
          </p>
        )}
      </div>

      <div className={styles.card}>
        <p className={styles.cardTitle}>Próximas ausências</p>
        {isLoadingAbsences && <Spinner />}
        {!isLoadingAbsences && upcomingAbsences.length === 0 && <EmptyState message="Sem ausências agendadas." />}
        {!isLoadingAbsences && upcomingAbsences.length > 0 && (
          <ul className={styles.list}>
            {upcomingAbsences.map((absence) => (
              <li key={absence.id} className={styles.listItem}>
                <span>{ABSENCE_TYPE_LABELS[absence.type] ?? absence.type}</span>
                <span className={styles.listItemMuted}>
                  {formatDate(absence.startDate)} – {formatDate(absence.endDate)}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

export default DashboardPage;