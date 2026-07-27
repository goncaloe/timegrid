import { useState, type MouseEvent } from 'react';
import Spinner from '../../components/Spinner';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import StatusHistory from '../../components/StatusHistory';
import StatusBadge from '../../components/StatusBadge';
import AbsenceModal from './AbsenceModal';
import { useAbsenceHistory, useAbsences, useCancelAbsence, useDeleteAbsence, useSubmitAbsence } from './hooks';
import type { AbsenceRequest } from './api';
import { formatDate } from '../../utils/date';
import { errorMessage } from '../../utils/errorMessage';
import styles from './AbsencesPage.module.css';

function isEditable(status: string) {
  return status === 'DRAFT' || status === 'REJECTED';
}

type ModalState = { mode: 'create' } | { mode: 'edit'; absence: AbsenceRequest } | null;

const TYPE_LABELS: Record<string, string> = {
  VACATION: 'Férias',
  SICK: 'Doença',
  PERSONAL: 'Assuntos pessoais',
  TRAINING: 'Formação',
  OTHER: 'Outro',
};

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Rascunho',
  SUBMITTED: 'Submetido',
  APPROVED: 'Aprovado',
  REJECTED: 'Rejeitado',
  CANCELLED: 'Cancelado',
};

function AbsencesPage() {
  const [year, setYear] = useState(() => new Date().getFullYear());
  const [modalState, setModalState] = useState<ModalState>(null);
  const [historyId, setHistoryId] = useState<string | null>(null);

  const { data: absences, isLoading, isError, error } = useAbsences(year);
  const { data: history } = useAbsenceHistory(historyId);
  const submitMutation = useSubmitAbsence();
  const cancelMutation = useCancelAbsence();
  const deleteMutation = useDeleteAbsence();

  function stopAnd(event: MouseEvent, action: () => void) {
    event.stopPropagation();
    action();
  }

  function handleDelete(id: string) {
    if (window.confirm('Eliminar este pedido de ausência?')) {
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
        <Button type="button" onClick={() => setModalState({ mode: 'create' })}>
          Novo pedido
        </Button>
      </div>

      {isLoading && <Spinner />}

      {isError && <ErrorState message={errorMessage(error)} />}

      {!isLoading && !isError && (absences ?? []).length === 0 && <EmptyState message="Sem pedidos de ausência neste ano." />}

      {!isLoading && !isError && (absences ?? []).length > 0 && (
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Início</th>
              <th>Fim</th>
              <th>Dias úteis</th>
              <th>Tipo</th>
              <th>Notas</th>
              <th>Estado</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {(absences ?? [])
              .slice()
              .sort((a, b) => a.startDate.localeCompare(b.startDate))
              .map((absence) => (
                <tr
                  key={absence.id}
                  onClick={() => isEditable(absence.status) && setModalState({ mode: 'edit', absence })}
                >
                  <td>{formatDate(absence.startDate)}</td>
                  <td>{formatDate(absence.endDate)}</td>
                  <td>{absence.workingDays}</td>
                  <td>{TYPE_LABELS[absence.type] ?? absence.type}</td>
                  <td className={styles.notesCell}>{absence.notes}</td>
                  <td>
                    <StatusBadge status={absence.status} label={STATUS_LABELS[absence.status] ?? absence.status} />
                  </td>
                  <td className={styles.actionsCell}>
                    {absence.allowedActions.includes('SUBMITTED') && (
                      <button
                        type="button"
                        className={styles.linkButton}
                        onClick={(event) => stopAnd(event, () => submitMutation.mutate(absence.id))}
                      >
                        Submeter
                      </button>
                    )}
                    {absence.allowedActions.includes('CANCELLED') && (
                      <button
                        type="button"
                        className={styles.linkButton}
                        onClick={(event) => stopAnd(event, () => cancelMutation.mutate(absence.id))}
                      >
                        Cancelar
                      </button>
                    )}
                    {isEditable(absence.status) && (
                      <button
                        type="button"
                        className={styles.linkButton}
                        onClick={(event) => stopAnd(event, () => handleDelete(absence.id))}
                      >
                        Eliminar
                      </button>
                    )}
                    <button
                      type="button"
                      className={styles.linkButton}
                      onClick={(event) => stopAnd(event, () => setHistoryId(absence.id))}
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
        <AbsenceModal
          absence={modalState.mode === 'edit' ? modalState.absence : undefined}
          onClose={() => setModalState(null)}
        />
      )}

      {historyId && (
        <Modal title="Histórico do pedido" onClose={() => setHistoryId(null)}>
          <StatusHistory changes={history ?? []} statusLabels={STATUS_LABELS} />
        </Modal>
      )}
    </div>
  );
}

export default AbsencesPage;