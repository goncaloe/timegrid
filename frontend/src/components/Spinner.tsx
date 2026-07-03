import styles from './Spinner.module.css';

function Spinner() {
  return <span className={styles.spinner} role="status" aria-label="A carregar" />;
}

export default Spinner;
