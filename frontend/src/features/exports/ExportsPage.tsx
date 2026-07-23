import { useState, type ChangeEvent, type FormEvent } from 'react';
import Button from '../../components/Button';
import { ApiError } from '../../api/client';
import { useExportFile } from './hooks';
import { KIND_LABELS, type ExportFormat, type ExportKind } from './api';
import styles from './ExportsPage.module.css';

const KIND_OPTIONS: { value: ExportKind; label: string }[] = [
  { value: 'time-entries', label: KIND_LABELS['time-entries'] },
  { value: 'absences', label: KIND_LABELS.absences },
  { value: 'summary', label: KIND_LABELS.summary },
];

const FORMAT_OPTIONS: { value: ExportFormat; label: string }[] = [
  { value: 'csv', label: 'CSV' },
  { value: 'xlsx', label: 'Excel (xlsx)' },
  { value: 'pdf', label: 'PDF' },
];

function ExportsPage() {
  const [kind, setKind] = useState<ExportKind>('time-entries');
  const [format, setFormat] = useState<ExportFormat>('csv');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [error, setError] = useState<string | null>(null);

  const exportMutation = useExportFile();

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      // TODO: mostrar o progresso do download
      await exportMutation.mutateAsync({ kind, from: from || null, to: to || null, format });
    } catch (err) {
      setError(err instanceof ApiError && err.problem.detail ? err.problem.detail : 'Não foi possível exportar.');
    }
  }

  return (
    <div>
      <h2 className={styles.title}>Exportações</h2>

      <form className={styles.card} onSubmit={handleSubmit}>
        <div className={styles.field}>
          <label className={styles.label} htmlFor="export-kind">
            Relatório
          </label>
          <select
            id="export-kind"
            className={styles.select}
            value={kind}
            onChange={(event: ChangeEvent<HTMLSelectElement>) => setKind(event.target.value as ExportKind)}
          >
            {KIND_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="export-from">
            De
          </label>
          <input
            id="export-from"
            type="date"
            className={styles.input}
            value={from}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setFrom(event.target.value)}
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="export-to">
            Até
          </label>
          <input
            id="export-to"
            type="date"
            className={styles.input}
            value={to}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setTo(event.target.value)}
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="export-format">
            Formato
          </label>
          <select
            id="export-format"
            className={styles.select}
            value={format}
            onChange={(event: ChangeEvent<HTMLSelectElement>) => setFormat(event.target.value as ExportFormat)}
          >
            {FORMAT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className={styles.actions}>
          <Button type="submit" disabled={exportMutation.isPending}>
            {exportMutation.isPending ? 'A gerar…' : 'Exportar'}
          </Button>
        </div>

        {error && <span className={styles.error}>{error}</span>}
        {exportMutation.isSuccess && !error && <span className={styles.saved}>Exportação descarregada.</span>}
      </form>

      <p className={styles.hint}>
        Sem datas, o relatório cobre o mês corrente. O resumo não tem filtros por utilizador ou departamento.
      </p>
    </div>
  );
}

export default ExportsPage;