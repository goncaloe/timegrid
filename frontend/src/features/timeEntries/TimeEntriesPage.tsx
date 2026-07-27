import { useMemo, useState, type MouseEvent } from 'react';
import Spinner from '../../components/Spinner';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import StatusHistory from '../../components/StatusHistory';
import EntryModal from './EntryModal';
import StatusBadge from '../../components/StatusBadge';
import { useDeleteTimeEntry, useSubmitTimeEntry, useTimeEntries, useTimeEntryHistory } from './hooks';
import type { TimeEntry } from './api';
import { addDays, formatWeekday, startOfWeek, toIsoDate } from '../../utils/date';
import { formatMinutes } from '../../utils/formatMinutes';
import { errorMessage } from '../../utils/errorMessage';
import styles from './TimeEntriesPage.module.css';

function isEditable(status: string) {
  return status === 'DRAFT' || status === 'REJECTED';
}

type ModalState = { mode: 'create' } | { mode: 'edit'; entry: TimeEntry } | null;

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

function TimeEntriesPage() {
  const [reference, setReference] = useState(() => new Date());
  const [modalState, setModalState] = useState<ModalState>(null);
  const [historyEntryId, setHistoryEntryId] = useState<string | null>(null);
  const weekStart = useMemo(() => startOfWeek(reference), [reference]);
  const weekEnd = useMemo(() => addDays(weekStart, 6), [weekStart]);
  const from = toIsoDate(weekStart);
  const to = toIsoDate(weekEnd);

  const { data: entries, isLoading, isError, error } = useTimeEntries(from, to);
  const { data: history } = useTimeEntryHistory(historyEntryId);
  const submitMutation = useSubmitTimeEntry();
  const deleteMutation = useDeleteTimeEntry();

  const total = (entries ?? []).reduce((sum, entry) => sum + entry.durationMinutes, 0);

  function stopAnd(event: MouseEvent, action: () => void) {
    event.stopPropagation();
    action();
  }

  function handleDelete(id: string) {
    if (window.confirm('Eliminar este registo?')) {
      deleteMutation.mutate(id);
    }
  }

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
          <span className={styles.summaryValue}>{formatMinutes(total)}</span>
        </div>
        <Button type="button" onClick={() => setModalState({ mode: 'create' })}>
          Novo registo
        </Button>
      </div>

      {isLoading && <Spinner />}

      {isError && <ErrorState message={errorMessage(error)} />}

      {!isLoading && !isError && (entries ?? []).length === 0 && (
        <EmptyState message="Sem registos nesta semana." />
      )}

      {!isLoading && !isError && (entries ?? []).length > 0 && (
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
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {(entries ?? [])
              .slice()
              .sort((a, b) => a.workDate.localeCompare(b.workDate))
              .map((entry) => (
                <tr
                  key={entry.id}
                  onClick={() => isEditable(entry.status) && setModalState({ mode: 'edit', entry })}
                >
                  <td className={styles.weekdayCell}>
                    {formatWeekday(new Date(`${entry.workDate}T00:00:00`))} {entry.workDate.slice(8, 10)}/
                    {entry.workDate.slice(5, 7)}
                  </td>
                  <td>{entry.startTime.slice(0, 5)}</td>
                  <td>{entry.endTime.slice(0, 5)}</td>
                  <td>{entry.breakMinutes} min</td>
                  <td>{formatMinutes(entry.durationMinutes)}</td>
                  <td>{TYPE_LABELS[entry.type] ?? entry.type}</td>
                  <td className={styles.notesCell}>{entry.notes}</td>
                  <td>
                    <StatusBadge status={entry.status} label={STATUS_LABELS[entry.status] ?? entry.status} />
                  </td>
                  <td className={styles.actionsCell}>
                    {entry.allowedActions.includes('SUBMITTED') && (
                      <button
                        type="button"
                        className={styles.linkButton}
                        onClick={(event) => stopAnd(event, () => submitMutation.mutate(entry.id))}
                      >
                        Submeter
                      </button>
                    )}
                    {isEditable(entry.status) && (
                      <button
                        type="button"
                        className={styles.linkButton}
                        onClick={(event) => stopAnd(event, () => handleDelete(entry.id))}
                      >
                        Eliminar
                      </button>
                    )}
                    <button
                      type="button"
                      className={styles.linkButton}
                      onClick={(event) => stopAnd(event, () => setHistoryEntryId(entry.id))}
                    >
                      Histórico
                    </button>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      )}

      {modalState && (
        <EntryModal
          entry={modalState.mode === 'edit' ? modalState.entry : undefined}
          onClose={() => setModalState(null)}
        />
      )}

      {historyEntryId && (
        <Modal title="Histórico do registo" onClose={() => setHistoryEntryId(null)}>
          <StatusHistory changes={history ?? []} statusLabels={STATUS_LABELS} />
        </Modal>
      )}
    </div>
  );
}

export default TimeEntriesPage;