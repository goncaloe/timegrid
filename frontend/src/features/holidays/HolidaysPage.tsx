import { useState, type ChangeEvent, type FormEvent } from 'react';
import Spinner from '../../components/Spinner';
import EmptyState from '../../components/EmptyState';
import Button from '../../components/Button';
import { ApiError } from '../../api/client';
import { useAuth } from '../../auth/useAuth';
import { useCreateHoliday, useDeleteHoliday, useHolidays } from './hooks';
import { toIsoDate } from '../../utils/date';
import styles from './HolidaysPage.module.css';

function HolidaysPage() {
  const { user } = useAuth();
  const canManage = user?.permissions.includes('holiday:write') ?? false;

  const [year, setYear] = useState(() => new Date().getFullYear());
  const [date, setDate] = useState(toIsoDate(new Date()));
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);

  const { data: holidays, isLoading } = useHolidays(year);
  const createMutation = useCreateHoliday();
  const deleteMutation = useDeleteHoliday();

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      await createMutation.mutateAsync({ date, name });
      setName('');
    } catch (err) {
      setError(err instanceof ApiError && err.problem.detail ? err.problem.detail : 'Não foi possível criar o feriado.');
    }
  }

  function handleDelete(id: string) {
    if (window.confirm('Eliminar este feriado?')) {
      deleteMutation.mutate(id);
    }
  }

  return (
    <div>
      <div className={styles.header}>
        <div className={styles.yearNav}>
          <button type="button" className={styles.navButton} onClick={() => setYear((current) => current - 1)}>
            ← {year - 1}
          </button>
          <span className={styles.yearLabel}>{year}</span>
          <button type="button" className={styles.navButton} onClick={() => setYear((current) => current + 1)}>
            {year + 1} →
          </button>
        </div>
      </div>

      {canManage && (
        <form className={styles.form} onSubmit={handleSubmit}>
          <div className={styles.formField}>
            <label htmlFor="holiday-date">Data</label>
            <input
              id="holiday-date"
              type="date"
              className={styles.formInput}
              value={date}
              onChange={(event: ChangeEvent<HTMLInputElement>) => setDate(event.target.value)}
              required
            />
          </div>
          <div className={styles.formField}>
            <label htmlFor="holiday-name">Nome</label>
            <input
              id="holiday-name"
              type="text"
              className={styles.formInput}
              value={name}
              maxLength={200}
              onChange={(event: ChangeEvent<HTMLInputElement>) => setName(event.target.value)}
              required
            />
          </div>
          <Button type="submit" disabled={createMutation.isPending}>
            Adicionar
          </Button>
          {error && <span className={styles.error}>{error}</span>}
        </form>
      )}

      {isLoading && <Spinner />}

      {!isLoading && (holidays ?? []).length === 0 && <EmptyState message="Sem feriados neste ano." />}

      {!isLoading && (holidays ?? []).length > 0 && (
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Data</th>
              <th>Nome</th>
              <th>Origem</th>
              {canManage && <th>Ações</th>}
            </tr>
          </thead>
          <tbody>
            {(holidays ?? [])
              .slice()
              .sort((a, b) => a.date.localeCompare(b.date))
              .map((holiday) => (
                <tr key={holiday.id ?? holiday.date}>
                  <td>{holiday.date.split('-').reverse().join('/')}</td>
                  <td>{holiday.name}</td>
                  <td>
                    <span className={`${styles.badge} ${holiday.national ? styles.national : styles.organization}`}>
                      {holiday.national ? 'Nacional' : 'Organização'}
                    </span>
                  </td>
                  {canManage && (
                    <td>
                      {!holiday.national && holiday.id && (
                        <button type="button" className={styles.linkButton} onClick={() => handleDelete(holiday.id as string)}>
                          Eliminar
                        </button>
                      )}
                    </td>
                  )}
                </tr>
              ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default HolidaysPage;