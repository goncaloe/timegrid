import styles from './Sidebar.module.css';

const NAV_ITEMS = ['Painel', 'Utilizadores'];

function Sidebar() {
  return (
    <aside className={styles.sidebar}>
      <div className={styles.brand}>TimeGrid</div>
      <nav className={styles.nav}>
        {NAV_ITEMS.map((item) => (
          <span key={item} className={styles.navItem}>
            {item}
          </span>
        ))}
      </nav>
    </aside>
  );
}

export default Sidebar;
