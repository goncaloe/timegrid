import styles from './ErrorState.module.css';

interface ErrorStateProps {
  message: string;
}

function ErrorState({ message }: ErrorStateProps) {
  return <div className={styles.wrapper}>{message}</div>;
}

export default ErrorState;