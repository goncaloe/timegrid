import styles from './StatusBadge.module.css';

const STATUS_CLASS: Record<string, string> = {
  DRAFT: styles.draft,
  SUBMITTED: styles.submitted,
  APPROVED: styles.approved,
  REJECTED: styles.rejected,
  CANCELLED: styles.cancelled,
};

interface StatusBadgeProps {
  status: string;
  label: string;
}

function StatusBadge({ status, label }: StatusBadgeProps) {
  const statusClass = STATUS_CLASS[status] ?? styles.draft;
  return <span className={`${styles.badge} ${statusClass}`}>{label}</span>;
}

export default StatusBadge;