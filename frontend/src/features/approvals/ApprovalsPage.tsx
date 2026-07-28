import { useState, type FormEvent } from 'react';
import Spinner from '../../components/Spinner';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import StatusBadge from '../../components/StatusBadge';
import TableRowActions from '../../components/TableRowActions';
import { ApiError } from '../../api/client';
import { startOfMonth, toIsoDate, formatDate } from '../../utils/date';
import { formatMinutes } from '../../utils/formatMinutes';
import { errorMessage } from '../../utils/errorMessage';
import { useUserTimeBank } from '../approvals/hooks';
import {
  useApproveAbsence,
  useApproveTimeEntry,
  usePendingAbsences,
  usePendingTimeEntries,
  useRejectAbsence,
  useRejectTimeEntry,
} from './hooks';
import type { AbsenceRequest } from '../absences/api';
import type { TimeEntry } from '../timeEntries/api';
import styles from './ApprovalsPage.module.css';

const TYPE_LABELS: Record<string, string> = {
  OFFICE: 'Escritório',
  REMOTE: 'Remoto',
  ON_SITE: 'Cliente',
  OVERTIME: 'Horas extra',
};

const ABSENCE_TYPE_LABELS: Record<string, string> = {
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

type Tab = 'timeEntries' | 'absences';

interface RejectTarget {
  kind: 'timeEntry' | 'absence';
  id: string;
  label: string;
}

function approveErrorMessage(error: unknown): string {
  return error instanceof ApiError && error.problem.detail ? error.problem.detail : 'Não foi possível aprovar.';
}

function ApprovalsPage() {
  const [tab, setTab] = useState<Tab>('timeEntries');
  const [rejectTarget, setRejectTarget] = useState<RejectTarget | null>(null);
  const [reason, setReason] = useState('');
  const [balanceUserId, setBalanceUserId] = useState<string | null>(null);

  const { data: timeEntries, isLoading: isLoadingTimeEntries, isError: isErrorTimeEntries, error: timeEntriesError } =
    usePendingTimeEntries();
  const { data: absences, isLoading: isLoadingAbsences, isError: isErrorAbsences, error: absencesError } =
    usePendingAbsences();

  const monthStart = startOfMonth(new Date());
  const monthFrom = toIsoDate(monthStart);
  const monthTo = toIsoDate(new Date());
  const { data: balance, isLoading: isLoadingBalance, isError: isErrorBalance, error: balanceError } =
    useUserTimeBank(balanceUserId, monthFrom, monthTo);

  const approveTimeEntry = useApproveTimeEntry();
  const rejectTimeEntry = useRejectTimeEntry();
  const approveAbsence = useApproveAbsence();
  const rejectAbsence = useRejectAbsence();

  function handleReject(event: FormEvent) {
    event.preventDefault();
    if (!rejectTarget || !reason.trim()) {
      return;
    }
    const payload = { id: rejectTarget.id, reason: reason.trim() };
    const mutation = rejectTarget.kind === 'timeEntry' ? rejectTimeEntry : rejectAbsence;
    mutation.mutate(payload, {
      onSuccess: () => {
        setRejectTarget(null);
        setReason('');
      },
    });
  }

  return (
    <div>
      <div className={styles.tabs}>
        <button
          type="button"
          className={`${styles.tab} ${tab === 'timeEntries' ? styles.tabActive : ''}`}
          onClick={() => setTab('timeEntries')}
        >
          Registos de horas
        </button>
        <button
          type="button"
          className={`${styles.tab} ${tab === 'absences' ? styles.tabActive : ''}`}
          onClick={() => setTab('absences')}
        >
          Ausências
        </button>
      </div>

      {tab === 'timeEntries' && (
        <section>
          {isLoadingTimeEntries && <Spinner />}
          {isErrorTimeEntries && <ErrorState message={errorMessage(timeEntriesError)} />}
          {!isLoadingTimeEntries && !isErrorTimeEntries && (timeEntries ?? []).length === 0 && (
            <EmptyState message="Sem registos de horas por aprovar." />
          )}
          {approveTimeEntry.isError && (
            <span className={styles.error}>{approveErrorMessage(approveTimeEntry.error)}</span>
          )}
          {!isLoadingTimeEntries && !isErrorTimeEntries && (timeEntries ?? []).length > 0 && (
            <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Colaborador</th>
                  <th>Data</th>
                  <th>Horário</th>
                  <th>Duração</th>
                  <th>Tipo</th>
                  <th>Notas</th>
                  <th>Estado</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {(timeEntries ?? [])
                  .slice()
                  .sort((a, b) => a.workDate.localeCompare(b.workDate))
                  .map((entry: TimeEntry) => (
                    <tr key={entry.id}>
                      <td>{entry.userFullName}</td>
                      <td>{formatDate(entry.workDate)}</td>
                      <td>
                        {entry.startTime.slice(0, 5)} – {entry.endTime.slice(0, 5)}
                      </td>
                      <td>{formatMinutes(entry.durationMinutes)}</td>
                      <td>{TYPE_LABELS[entry.type] ?? entry.type}</td>
                      <td className={styles.notesCell}>{entry.notes}</td>
                      <td>
                        <StatusBadge status={entry.status} label={STATUS_LABELS[entry.status] ?? entry.status} />
                      </td>
                      <TableRowActions>
                        {entry.allowedActions.includes('APPROVED') && (
                          <button
                            type="button"
                            className={styles.approveButton}
                            onClick={() => approveTimeEntry.mutate(entry.id)}
                            disabled={approveTimeEntry.isPending}
                          >
                            Aprovar
                          </button>
                        )}
                        <button type="button" className={styles.linkButton} onClick={() => setBalanceUserId(entry.userId)}>
                          Saldo
                        </button>
                        {entry.allowedActions.includes('REJECTED') && (
                          <button
                            type="button"
                            className={styles.dangerLink}
                            onClick={() =>
                              setRejectTarget({
                                kind: 'timeEntry',
                                id: entry.id,
                                label: `registo de ${entry.userFullName} de ${formatDate(entry.workDate)}`,
                              })
                            }
                          >
                            Rejeitar
                          </button>
                        )}
                      </TableRowActions>
                    </tr>
                  ))}
              </tbody>
            </table>
            </div>
          )}
        </section>
      )}

      {tab === 'absences' && (
        <section>
          {isLoadingAbsences && <Spinner />}
          {isErrorAbsences && <ErrorState message={errorMessage(absencesError)} />}
          {!isLoadingAbsences && !isErrorAbsences && (absences ?? []).length === 0 && (
            <EmptyState message="Sem pedidos de ausência por aprovar." />
          )}
          {approveAbsence.isError && <span className={styles.error}>{approveErrorMessage(approveAbsence.error)}</span>}
          {!isLoadingAbsences && !isErrorAbsences && (absences ?? []).length > 0 && (
            <div className={styles.tableWrap}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Colaborador</th>
                  <th>Período</th>
                  <th>Dias úteis</th>
                  <th>Tipo</th>
                  <th>Estado</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {(absences ?? [])
                  .slice()
                  .sort((a, b) => a.startDate.localeCompare(b.startDate))
                  .map((absence: AbsenceRequest) => (
                    <tr key={absence.id}>
                      <td>{absence.userFullName}</td>
                      <td>
                        {formatDate(absence.startDate)} – {formatDate(absence.endDate)}
                      </td>
                      <td>{absence.workingDays}</td>
                      <td>{ABSENCE_TYPE_LABELS[absence.type] ?? absence.type}</td>
                      <td>
                        <StatusBadge status={absence.status} label={STATUS_LABELS[absence.status] ?? absence.status} />
                      </td>
                      <TableRowActions>
                        {absence.allowedActions.includes('APPROVED') && (
                          <button
                            type="button"
                            className={styles.approveButton}
                            onClick={() => approveAbsence.mutate(absence.id)}
                            disabled={approveAbsence.isPending}
                          >
                            Aprovar
                          </button>
                        )}
                        <button type="button" className={styles.linkButton} onClick={() => setBalanceUserId(absence.userId)}>
                          Saldo
                        </button>
                        {absence.allowedActions.includes('REJECTED') && (
                          <button
                            type="button"
                            className={styles.dangerLink}
                            onClick={() =>
                              setRejectTarget({
                                kind: 'absence',
                                id: absence.id,
                                label: `ausência de ${absence.userFullName} de ${formatDate(absence.startDate)}`,
                              })
                            }
                          >
                            Rejeitar
                          </button>
                        )}
                      </TableRowActions>
                    </tr>
                  ))}
              </tbody>
            </table>
            </div>
          )}
        </section>
      )}

      {balanceUserId && (
        <Modal title="Banco de horas do colaborador" onClose={() => setBalanceUserId(null)}>
          {isLoadingBalance && <Spinner />}
          {isErrorBalance && <ErrorState message={errorMessage(balanceError)} />}
          {!isLoadingBalance && !isErrorBalance && balance && (
            <div className={styles.balance}>
              <span className={styles.balanceMuted}>
                {formatDate(monthFrom)} — {formatDate(monthTo)}
              </span>
              <span className={`${styles.balanceValue} ${balance.balanceMinutes >= 0 ? styles.positive : styles.negative}`}>
                {formatMinutes(balance.balanceMinutes)}
              </span>
              <span className={styles.balanceMuted}>
                Esperado {formatMinutes(balance.expectedMinutes)} · Trabalhado {formatMinutes(balance.workedMinutes)}
              </span>
            </div>
          )}
        </Modal>
      )}

      {rejectTarget && (
        <Modal title={`Rejeitar ${rejectTarget.label}`} onClose={() => setRejectTarget(null)}>
          <form onSubmit={handleReject}>
            <div className={styles.formField}>
              <label className={styles.label} htmlFor="reject-reason">
                Motivo
              </label>
              <textarea
                id="reject-reason"
                className={styles.textarea}
                value={reason}
                maxLength={500}
                required
                onChange={(event) => setReason(event.target.value)}
                placeholder="Indica o motivo da rejeição"
              />
            </div>
            <div className={styles.actions}>
              <Button type="button" className={styles.secondaryButton} onClick={() => setRejectTarget(null)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={rejectTimeEntry.isPending || rejectAbsence.isPending}>
                Rejeitar
              </Button>
            </div>
            {(rejectTimeEntry.isError || rejectAbsence.isError) && (
              <span className={styles.error}>
                {(() => {
                  const error = rejectTimeEntry.error ?? rejectAbsence.error;
                  return error instanceof ApiError && error.problem.detail
                    ? error.problem.detail
                    : 'Não foi possível rejeitar.';
                })()}
              </span>
            )}
          </form>
        </Modal>
      )}
    </div>
  );
}

export default ApprovalsPage;