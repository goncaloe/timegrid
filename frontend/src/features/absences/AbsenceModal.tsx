import { useState, type ChangeEvent, type FormEvent } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import Field from '../../components/Field';
import { ApiError } from '../../api/client';
import { useCreateAbsence, useUpdateAbsence } from './hooks';
import type { AbsenceRequest, AbsenceRequestInput, AbsenceType } from './api';
import { toIsoDate } from '../../utils/date';
import styles from '../../components/FormModal.module.css';

const TYPE_OPTIONS: { value: AbsenceType; label: string }[] = [
  { value: 'VACATION', label: 'Férias' },
  { value: 'SICK', label: 'Doença' },
  { value: 'PERSONAL', label: 'Assuntos pessoais' },
  { value: 'TRAINING', label: 'Formação' },
  { value: 'OTHER', label: 'Outro' },
];

interface AbsenceModalProps {
  absence?: AbsenceRequest;
  onClose: () => void;
}

function AbsenceModal({ absence, onClose }: AbsenceModalProps) {
  const isEditing = absence !== undefined;
  const createMutation = useCreateAbsence();
  const updateMutation = useUpdateAbsence();

  const [startDate, setStartDate] = useState(absence?.startDate ?? toIsoDate(new Date()));
  const [endDate, setEndDate] = useState(absence?.endDate ?? toIsoDate(new Date()));
  const [type, setType] = useState<AbsenceType>(absence?.type ?? 'VACATION');
  const [notes, setNotes] = useState(absence?.notes ?? '');
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setFieldErrors({});

    const input: AbsenceRequestInput = { startDate, endDate, type, notes };

    try {
      if (isEditing) {
        await updateMutation.mutateAsync({ id: absence.id, input });
      } else {
        await createMutation.mutateAsync(input);
      }
      onClose();
    } catch (err) {
      if (err instanceof ApiError) {
        setFieldErrors(err.problem.errors ?? {});
        setError(err.problem.errors ? null : (err.problem.detail ?? 'Não foi possível guardar o pedido.'));
      } else {
        setError('Não foi possível guardar o pedido.');
      }
    }
  }

  return (
    <Modal title={isEditing ? 'Editar ausência' : 'Novo pedido de ausência'} onClose={onClose}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.row}>
          <Field
            label="Data de início"
            name="startDate"
            type="date"
            value={startDate}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setStartDate(event.target.value)}
            error={fieldErrors.startDate}
            required
          />
          <Field
            label="Data de fim"
            name="endDate"
            type="date"
            value={endDate}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setEndDate(event.target.value)}
            error={fieldErrors.endDate}
            required
          />
        </div>
        <div className={styles.field}>
          <label className={styles.label} htmlFor="type">
            Tipo
          </label>
          <select
            id="type"
            className={styles.select}
            value={type}
            onChange={(event: ChangeEvent<HTMLSelectElement>) => setType(event.target.value as AbsenceType)}
          >
            {TYPE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
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

export default AbsenceModal;