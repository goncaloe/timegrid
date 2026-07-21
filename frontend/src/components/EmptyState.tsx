import styles from './EmptyState.module.css';

interface EmptyStateProps {
  message: string;
}

function EmptyState({ message }: EmptyStateProps) {
  return <div className={styles.wrapper}>{message}</div>;
}

export default EmptyState;