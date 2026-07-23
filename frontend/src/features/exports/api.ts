import { downloadFile, type DownloadResult } from '../../api/client';

export type ExportKind = 'time-entries' | 'absences' | 'summary';
export type ExportFormat = 'csv' | 'xlsx' | 'pdf';

const KIND_LABELS: Record<ExportKind, string> = {
  'time-entries': 'Registos de horas',
  absences: 'Ausências',
  summary: 'Resumo por colaborador',
};

export interface ExportRequest {
  kind: ExportKind;
  from: string | null;
  to: string | null;
  format: ExportFormat;
}

export function exportFile({ kind, from, to, format }: ExportRequest): Promise<DownloadResult> {
  const params = new URLSearchParams({ format });
  if (from) {
    params.set('from', from);
  }
  if (to) {
    params.set('to', to);
  }
  return downloadFile(`/exports/${kind}?${params.toString()}`);
}

export { KIND_LABELS };