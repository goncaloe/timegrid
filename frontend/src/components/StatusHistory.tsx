import type { StatusChange } from '../api/workflow';
import EmptyState from './EmptyState';
import styles from './StatusHistory.module.css';

interface StatusHistoryProps {
  changes: StatusChange[];
  statusLabels: Record<string, string>;
}

function StatusHistory({ changes, statusLabels }: StatusHistoryProps) {
  if (changes.length === 0) {
    return <EmptyState message="Sem alterações de estado." />;
  }

  return (
    <ul className={styles.list}>
      {changes.map((change) => (
        <li key={change.id} className={styles.item}>
          <span className={styles.transition}>
            {change.fromStatus ? `${statusLabels[change.fromStatus] ?? change.fromStatus} → ` : ''}
            {statusLabels[change.toStatus] ?? change.toStatus}
          </span>
          <span className={styles.meta}>{new Date(change.changedAt).toLocaleString('pt-PT')}</span>
          {change.comment && <span className={styles.comment}>{change.comment}</span>}
        </li>
      ))}
    </ul>
  );
}

export default StatusHistory;