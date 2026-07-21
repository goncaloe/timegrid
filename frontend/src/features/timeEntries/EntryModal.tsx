import { useState, type ChangeEvent, type FormEvent } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import Field from '../../components/Field';
import { ApiError } from '../../api/client';
import { useCreateTimeEntry, useUpdateTimeEntry } from './hooks';
import type { TimeEntry, TimeEntryInput, TimeEntryType } from './api';
import { toIsoDate } from '../../utils/date';
import styles from '../../components/FormModal.module.css';

const TYPE_OPTIONS: { value: TimeEntryType; label: string }[] = [
  { value: 'OFFICE', label: 'Escritório' },
  { value: 'REMOTE', label: 'Remoto' },
  { value: 'ON_SITE', label: 'Cliente' },
  { value: 'OVERTIME', label: 'Horas extra' },
];

interface EntryModalProps {
  entry?: TimeEntry;
  onClose: () => void;
}

function EntryModal({ entry, onClose }: EntryModalProps) {
  const isEditing = entry !== undefined;
  const createMutation = useCreateTimeEntry();
  const updateMutation = useUpdateTimeEntry();

  const [workDate, setWorkDate] = useState(entry?.workDate ?? toIsoDate(new Date()));
  const [startTime, setStartTime] = useState(entry?.startTime.slice(0, 5) ?? '09:00');
  const [endTime, setEndTime] = useState(entry?.endTime.slice(0, 5) ?? '18:00');
  const [breakMinutes, setBreakMinutes] = useState(entry?.breakMinutes ?? 60);
  const [type, setType] = useState<TimeEntryType>(entry?.type ?? 'OFFICE');
  const [notes, setNotes] = useState(entry?.notes ?? '');
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setFieldErrors({});

    const input: TimeEntryInput = { workDate, startTime, endTime, breakMinutes, type, notes };

    try {
      if (isEditing) {
        await updateMutation.mutateAsync({ id: entry.id, input });
      } else {
        await createMutation.mutateAsync(input);
      }
      onClose();
    } catch (err) {
      if (err instanceof ApiError) {
        setFieldErrors(err.problem.errors ?? {});
        setError(err.problem.errors ? null : (err.problem.detail ?? 'Não foi possível guardar o registo.'));
      } else {
        setError('Não foi possível guardar o registo.');
      }
    }
  }

  return (
    <Modal title={isEditing ? 'Editar registo' : 'Novo registo'} onClose={onClose}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <Field
          label="Data"
          name="workDate"
          type="date"
          value={workDate}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setWorkDate(event.target.value)}
          error={fieldErrors.workDate}
          required
        />
        <div className={styles.row}>
          <Field
            label="Início"
            name="startTime"
            type="time"
            value={startTime}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setStartTime(event.target.value)}
            error={fieldErrors.startTime}
            required
          />
          <Field
            label="Fim"
            name="endTime"
            type="time"
            value={endTime}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setEndTime(event.target.value)}
            error={fieldErrors.endTime}
            required
          />
        </div>
        <div className={styles.row}>
          <Field
            label="Pausa (minutos)"
            name="breakMinutes"
            type="number"
            min={0}
            value={breakMinutes}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setBreakMinutes(Number(event.target.value))}
            error={fieldErrors.breakMinutes}
          />
          <div className={styles.field}>
            <label className={styles.label} htmlFor="type">
              Tipo
            </label>
            <select
              id="type"
              className={styles.select}
              value={type}
              onChange={(event: ChangeEvent<HTMLSelectElement>) => setType(event.target.value as TimeEntryType)}
            >
              {TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
        <div className={styles.field}>
          <label className={styles.label} htmlFor="notes">
            Notas
          </label>
          <textarea
            id="notes"
            className={styles.textarea}
            value={notes}
            maxLength={500}
            onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setNotes(event.target.value)}
          />
          {fieldErrors.notes && <span className={styles.error}>{fieldErrors.notes}</span>}
        </div>

        {error && <span className={styles.error}>{error}</span>}

        <div className={styles.actions}>
          <Button type="button" className={styles.secondaryButton} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            Guardar
          </Button>
        </div>
      </form>
    </Modal>
  );
}

export default EntryModal;