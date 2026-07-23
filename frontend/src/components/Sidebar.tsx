import { NavLink } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';
import styles from './Sidebar.module.css';

interface NavItem {
  label: string;
  path: string;
  permissions?: string[];
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Painel', path: '/' },
  { label: 'Registos de horas', path: '/time-entries' },
  { label: 'Ausências', path: '/absences' },
  { label: 'Feriados', path: '/holidays' },
  { label: 'Aprovações', path: '/approvals', permissions: ['time_entry:approve', 'absence:approve'] },
  { label: 'Utilizadores', path: '/users', permissions: ['user:write'] },
  { label: 'Departamentos', path: '/teams', permissions: ['team:write'] },
  { label: 'Organização', path: '/organization', permissions: ['organization:write'] },
  { label: 'Exportações', path: '/exports', permissions: ['report:export'] },
];

function Sidebar() {
  const { user } = useAuth();
  const permissions = user?.permissions ?? [];

  const items = NAV_ITEMS.filter(
    (item) => !item.permissions || item.permissions.some((permission) => permissions.includes(permission)),
  );

  return (
    <aside className={styles.sidebar}>
      <div className={styles.brand}>TimeGrid</div>
      <nav className={styles.nav}>
        {items.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === '/'}
            className={({ isActive }) => (isActive ? `${styles.navItem} ${styles.navItemActive}` : styles.navItem)}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}

export default Sidebar;