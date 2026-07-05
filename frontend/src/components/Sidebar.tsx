import { NavLink } from 'react-router-dom';
import styles from './Sidebar.module.css';

interface NavItem {
  label: string;
  path: string;
  permissions?: string[];
}

const NAV_ITEMS: NavItem[] = [
  { label: 'Painel', path: '/' },
  { label: 'Utilizadores', path: '/users' },
  { label: 'Departamentos', path: '/teams' },
  { label: 'Organização', path: '/organization' },
];

function Sidebar() {
  return (
    <aside className={styles.sidebar}>
      <div className={styles.brand}>TimeGrid</div>
      <nav className={styles.nav}>
        {NAV_ITEMS.map((item) => (
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
