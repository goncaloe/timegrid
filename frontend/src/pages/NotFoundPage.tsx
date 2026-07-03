import { Link } from 'react-router-dom';
import styles from './NotFoundPage.module.css';

function NotFoundPage() {
  return (
    <div className={styles.wrapper}>
      <h1>404</h1>
      <p>Página não encontrada.</p>
      <Link to="/">Voltar ao painel</Link>
    </div>
  );
}

export default NotFoundPage;
