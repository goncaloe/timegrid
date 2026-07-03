import { useState, type ChangeEvent, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../components/Button';
import Field from '../components/Field';
import Spinner from '../components/Spinner';
import { ApiError } from '../api/client';
import { useAuth } from '../auth/useAuth';
import styles from './LoginPage.module.css';

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    console.log('login', username);
    setError(null);
    setIsSubmitting(true);
    try {
      await login({ username, password });
      navigate('/', { replace: true });
    } catch (err) {
      setError(err instanceof ApiError && err.problem.detail ? err.problem.detail : 'Não foi possível iniciar sessão.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className={styles.wrapper}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <h1>TimeGrid</h1>
        <Field
          label="Utilizador"
          name="username"
          autoComplete="username"
          value={username}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setUsername(event.target.value)}
          required
        />
        <Field
          label="Palavra-passe"
          name="password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setPassword(event.target.value)}
          required
        />
        {error && <p className={styles.error}>{error}</p>}
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? <Spinner /> : 'Iniciar sessão'}
        </Button>
      </form>
    </div>
  );
}

export default LoginPage;
