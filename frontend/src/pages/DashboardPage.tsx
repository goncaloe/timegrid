import { useMemo } from 'react';
import Spinner from '../components/Spinner';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import { useTimeBankMe } from '../features/timebank/hooks';
import { useTimeEntries } from '../features/timeEntries/hooks';
import { useAbsences } from '../features/absences/hooks';
import { useHolidays } from '../features/holidays/hooks';
import { addDays, formatDate, startOfMonth, startOfWeek, toIsoDate } from '../utils/date';
import { formatMinutes } from '../utils/formatMinutes';
import { errorMessage } from '../utils/errorMessage';
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
  const monthTo = toIsoDate(today);
  const weekStart = startOfWeek(today);
  const weekFrom = toIsoDate(weekStart);
  const weekTo = toIsoDate(addDays(weekStart, 6));
  const todayIso = toIsoDate(today);

  const { data: balance, isLoading: isLoadingBalance, isError: isErrorBalance, error: balanceError } =
    useTimeBankMe(monthFrom, monthTo);
  const { data: weekEntries, isLoading: isLoadingWeek, isError: isErrorWeek, error: weekError } =
    useTimeEntries(weekFrom, weekTo);
  const { data: absences, isLoading: isLoadingAbsences, isError: isErrorAbsences, error: absencesError } =
    useAbsences(today.getFullYear());
  const { data: holidays, isLoading: isLoadingHolidays, isError: isErrorHolidays, error: holidaysError } =
    useHolidays(today.getFullYear());

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
        {isErrorBalance && <ErrorState message={errorMessage(balanceError)} />}
        {!isLoadingBalance && !isErrorBalance && balance && (
          <p className={`${styles.cardValue} ${balance.balanceMinutes >= 0 ? styles.positive : styles.negative}`}>
            {formatMinutes(balance.balanceMinutes)}
          </p>
        )}
      </div>

      <div className={styles.card}>
        <p className={styles.cardTitle}>Horas esta semana</p>
        {isLoadingWeek && <Spinner />}
        {isErrorWeek && <ErrorState message={errorMessage(weekError)} />}
        {!isLoadingWeek && !isErrorWeek && <p className={styles.cardValue}>{formatMinutes(weekTotal)}</p>}
      </div>

      <div className={styles.card}>
        <p className={styles.cardTitle}>Próximo feriado</p>
        {isLoadingHolidays && <Spinner />}
        {isErrorHolidays && <ErrorState message={errorMessage(holidaysError)} />}
        {!isLoadingHolidays && !isErrorHolidays && !nextHoliday && <EmptyState message="Sem feriados agendados." />}
        {!isLoadingHolidays && !isErrorHolidays && nextHoliday && (
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
        {isErrorAbsences && <ErrorState message={errorMessage(absencesError)} />}
        {!isLoadingAbsences && !isErrorAbsences && upcomingAbsences.length === 0 && (
          <EmptyState message="Sem ausências agendadas." />
        )}
        {!isLoadingAbsences && !isErrorAbsences && upcomingAbsences.length > 0 && (
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