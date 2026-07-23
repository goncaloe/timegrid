import { useMutation } from '@tanstack/react-query';
import { exportFile, type ExportRequest } from './api';

function triggerDownload(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

export function useExportFile() {
  return useMutation({
    mutationFn: (request: ExportRequest) => exportFile(request),
    onSuccess: ({ blob, filename }) => triggerDownload(blob, filename),
  });
}