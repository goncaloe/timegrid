import type { InputHTMLAttributes } from 'react';
import styles from './Field.module.css';

interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

function Field({ label, error, id, name, ...props }: FieldProps) {
  const fieldId = id ?? name;
  return (
    <div className={styles.field}>
      <label htmlFor={fieldId} className={styles.label}>
        {label}
      </label>
      <input id={fieldId} name={name} className={styles.input} {...props} />
      {error && <span className={styles.error}>{error}</span>}
    </div>
  );
}

export default Field;
