import Button from './Button';
import NotificationBell from './NotificationBell';
import { useAuth } from '../auth/useAuth';
import styles from './Topbar.module.css';

function Topbar() {
  const { user, logout } = useAuth();

  return (
    <header className={styles.topbar}>
      {user && (
        <div className={styles.session}>
          <NotificationBell />
          <span className={styles.userName}>{user.fullName}</span>
          <Button type="button" onClick={() => logout()}>
            Terminar sessão
          </Button>
        </div>
      )}
    </header>
  );
}

export default Topbar;