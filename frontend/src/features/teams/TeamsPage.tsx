import { useState, type ChangeEvent, type FormEvent } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import Field from '../../components/Field';
import Spinner from '../../components/Spinner';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import { ApiError } from '../../api/client';
import { useAuth } from '../../auth/useAuth';
import { useCreateTeam, useDeleteTeam, useTeams, useUpdateTeam } from './hooks';
import type { Team } from './api';
import { errorMessage } from '../../utils/errorMessage';
import styles from './TeamsPage.module.css';

interface TeamModalState {
  mode: 'create' | 'edit';
  team?: Team;
}

function TeamsPage() {
  const { user } = useAuth();
  const canManage = user?.permissions.includes('team:write') ?? false;
  const [modalState, setModalState] = useState<TeamModalState | null>(null);

  const { data: teams, isLoading, isError, error } = useTeams();
  const deleteMutation = useDeleteTeam();

  function handleDelete(team: Team) {
    if (window.confirm(`Eliminar o departamento ${team.name}?`)) {
      deleteMutation.mutate(team.id);
    }
  }

  return (
    <div>
      <div className={styles.header}>
        <h2 className={styles.title}>Departamentos</h2>
        {canManage && (
          <Button type="button" onClick={() => setModalState({ mode: 'create' })}>
            Novo departamento
          </Button>
        )}
      </div>

      {isLoading && <Spinner />}

      {isError && <ErrorState message={errorMessage(error)} />}

      {!isLoading && !isError && (teams ?? []).length === 0 && <EmptyState message="Sem departamentos nesta organização." />}

      {!isLoading && !isError && (teams ?? []).length > 0 && (
        <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Descrição</th>
              {canManage && <th>Ações</th>}
            </tr>
          </thead>
          <tbody>
            {(teams ?? []).map((team) => (
              <tr key={team.id}>
                <td>{team.name}</td>
                <td className={styles.notesCell}>{team.description}</td>
                {canManage && (
                  <td className={styles.actionsCell}>
                    <button type="button" className={styles.linkButton} onClick={() => setModalState({ mode: 'edit', team })}>
                      Editar
                    </button>
                    <button type="button" className={styles.dangerLink} onClick={() => handleDelete(team)}>
                      Eliminar
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
        </div>
      )}

      {modalState && canManage && (
        <TeamModal
          key={modalState.mode === 'edit' ? modalState.team?.id : 'create'}
          team={modalState.mode === 'edit' ? modalState.team : undefined}
          onClose={() => setModalState(null)}
        />
      )}
    </div>
  );
}

interface TeamModalProps {
  team?: Team;
  onClose: () => void;
}

function TeamModal({ team, onClose }: TeamModalProps) {
  const isEditing = team !== undefined;
  const createMutation = useCreateTeam();
  const updateMutation = useUpdateTeam();

  const [name, setName] = useState(team?.name ?? '');
  const [description, setDescription] = useState(team?.description ?? '');
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const input = { name, description };
      if (isEditing && team) {
        await updateMutation.mutateAsync({ id: team.id, input });
      } else {
        await createMutation.mutateAsync(input);
      }
      onClose();
    } catch (err) {
      setError(err instanceof ApiError && err.problem.detail ? err.problem.detail : 'Não foi possível guardar o departamento.');
    }
  }

  return (
    <Modal title={isEditing ? `Editar ${team?.name}` : 'Novo departamento'} onClose={onClose}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <Field
          label="Nome"
          name="team-name"
          value={name}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setName(event.target.value)}
          required
        />
        <Field
          label="Descrição"
          name="team-description"
          value={description}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setDescription(event.target.value)}
        />
        {error && <span className={styles.error}>{error}</span>}
        <div className={styles.actions}>
          <Button type="button" className={styles.secondaryButton} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" disabled={createMutation.isPending || updateMutation.isPending}>
            Guardar
          </Button>
        </div>
      </form>
    </Modal>
  );
}

export default TeamsPage;