import type { ReactNode } from 'react';
import Sidebar from './Sidebar';
import Topbar from './Topbar';
import styles from './Layout.module.css';

interface LayoutProps {
  children: ReactNode;
}

function Layout({ children }: LayoutProps) {
  return (
    <div className={styles.shell}>
      <Sidebar />
      <div className={styles.main}>
        <Topbar />
        <main className={styles.content}>{children}</main>
      </div>
    </div>
  );
}

export default Layout;
