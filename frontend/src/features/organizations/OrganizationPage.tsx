import { useState, type ChangeEvent, type FormEvent } from 'react';
import Button from '../../components/Button';
import Field from '../../components/Field';
import Spinner from '../../components/Spinner';
import { ApiError } from '../../api/client';
import { useAuth } from '../../auth/useAuth';
import { useOrganization, useUpdateOrganization } from './hooks';
import styles from './OrganizationPage.module.css';

const TIMEZONES = [
  'Europe/Lisbon',
  'Atlantic/Azores',
  'Atlantic/Madeira',
  'Atlantic/Cape_Verde',
];

function OrganizationPage() {
  const { user } = useAuth();
  const canManage = user?.permissions.includes('organization:write') ?? false;

  const { data: organization, isLoading } = useOrganization();
  const updateMutation = useUpdateOrganization();

  const [name, setName] = useState('');
  const [timezone, setTimezone] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  if (isLoading) {
    return <Spinner />;
  }

  if (!organization) {
    return null;
  }

  const org = organization;

  // cada campo cai para o valor atual da organização quando não foi tocado,
  // para editar só o nome (ou só o timezone) não apagar o outro no pedido
  const currentName = name || org.name;
  const currentTimezone = timezone || org.timezone;

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSaved(false);
    updateMutation.mutate(
      { id: org.id, input: { name: currentName, timezone: currentTimezone } },
      {
        onSuccess: () => setSaved(true),
        onError: (err) =>
          setError(err instanceof ApiError && err.problem.detail ? err.problem.detail : 'Não foi possível guardar.'),
      },
    );
  }

  return (
    <div>
      <h2 className={styles.title}>Organização</h2>

      <form className={styles.form} onSubmit={handleSubmit}>
        <Field
          label="Nome"
          name="org-name"
          value={currentName}
          onChange={(event: ChangeEvent<HTMLInputElement>) => {
            setName(event.target.value);
            setSaved(false);
          }}
          required
          disabled={!canManage}
        />
        <div className={styles.field}>
          <label className={styles.label} htmlFor="org-timezone">
            Fuso horário
          </label>
          <select
            id="org-timezone"
            className={styles.select}
            value={currentTimezone}
            onChange={(event: ChangeEvent<HTMLSelectElement>) => {
              setTimezone(event.target.value);
              setSaved(false);
            }}
            disabled={!canManage}
          >
            {TIMEZONES.map((tz) => (
              <option key={tz} value={tz}>
                {tz}
              </option>
            ))}
          </select>
        </div>

        {error && <span className={styles.error}>{error}</span>}
        {saved && <span className={styles.saved}>Guardado.</span>}

        {canManage && (
          <div>
            <Button type="submit" disabled={updateMutation.isPending}>
              Guardar
            </Button>
          </div>
        )}
      </form>

      <p className={styles.muted}>{currentName} · {currentTimezone}</p>
    </div>
  );
}

export default OrganizationPage;