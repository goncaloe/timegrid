import type { ReactNode } from 'react';
import styles from './TableRowActions.module.css';

interface TableRowActionsProps {
  children: ReactNode;
}

function TableRowActions({ children }: TableRowActionsProps) {
  return (
    <td className={styles.cell}>
      <div className={styles.inner}>{children}</div>
    </td>
  );
}

export default TableRowActions;