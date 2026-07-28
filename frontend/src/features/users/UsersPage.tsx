import { useState, type ChangeEvent, type FormEvent } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import Field from '../../components/Field';
import Spinner from '../../components/Spinner';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import TableRowActions from '../../components/TableRowActions';
import { ApiError } from '../../api/client';
import { useAuth } from '../../auth/useAuth';
import { useTeams } from '../teams/hooks';
import { useRoles } from '../access/hooks';
import { useCreateUser, useDeleteUser, useRestoreUser, useUpdateUserDailyWorkMinutes, useUpdateUserRoles, useUpdateUserTeam, useUsers } from './hooks';
import type { User } from './api';
import { errorMessage } from '../../utils/errorMessage';
import styles from './UsersPage.module.css';

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Ativo',
  INACTIVE: 'Inativo',
  LOCKED: 'Bloqueado',
};

const PAGE_SIZE = 20;

interface EditState {
  mode: 'create' | 'edit';
  user?: User;
}

function UsersPage() {
  const { user: currentUser } = useAuth();
  const canManage = currentUser?.permissions.includes('user:write') ?? false;

  const [page, setPage] = useState(0);
  const [editState, setEditState] = useState<EditState | null>(null);

  const { data: pageData, isLoading, isError, error: loadError } = useUsers(page, PAGE_SIZE);
  const { data: teams } = useTeams();
  const { data: roles } = useRoles();

  const deleteMutation = useDeleteUser();
  const restoreMutation = useRestoreUser();

  const users = pageData?.content ?? [];

  function handleDelete(user: User) {
    if (window.confirm(`Desativar o utilizador ${user.fullName}?`)) {
      deleteMutation.mutate(user.id);
    }
  }

  return (
    <div>
      <div className={styles.header}>
        <h2 className={styles.title}>Utilizadores</h2>
        {canManage && (
          <Button type="button" onClick={() => setEditState({ mode: 'create' })}>
            Novo utilizador
          </Button>
        )}
      </div>

      {isLoading && <Spinner />}

      {isError && <ErrorState message={errorMessage(loadError)} />}

      {!isLoading && !isError && users.length === 0 && <EmptyState message="Sem utilizadores." />}

      {!isLoading && !isError && users.length > 0 && (
        <>
          <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Utilizador</th>
                <th>Email</th>
                <th>Departamento</th>
                <th>Perfis</th>
                <th>Estado</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>{user.fullName}</td>
                  <td>{user.username}</td>
                  <td>{user.email}</td>
                  <td>{user.teamId ? (teams ?? []).find((team) => team.id === user.teamId)?.name ?? '—' : '—'}</td>
                  <td>{user.roles.join(', ')}</td>
                  <td>
                    <span className={`${styles.statusBadge} ${user.status === 'ACTIVE' ? styles.statusActive : styles.statusInactive}`}>
                      {STATUS_LABELS[user.status] ?? user.status}
                    </span>
                  </td>
                  <TableRowActions>
                    {canManage && (
                      <>
                        {user.id !== currentUser?.userId && (
                          <button
                            type="button"
                            className={styles.linkButton}
                            onClick={() => setEditState({ mode: 'edit', user })}
                          >
                            Editar
                          </button>
                        )}
                        {user.status === 'ACTIVE' ? (
                          <button type="button" className={styles.dangerLink} onClick={() => handleDelete(user)}>
                            Desativar
                          </button>
                        ) : (
                          <button type="button" className={styles.linkButton} onClick={() => restoreMutation.mutate(user.id)}>
                            Restaurar
                          </button>
                        )}
                      </>
                    )}
                  </TableRowActions>
                </tr>
              ))}
            </tbody>
          </table>
          </div>

          {pageData && pageData.totalPages > 1 && (
            <div className={styles.pagination}>
              <button
                type="button"
                className={styles.navButton}
                disabled={page === 0}
                onClick={() => setPage((current) => current - 1)}
              >
                ← anteriores
              </button>
              <span className={styles.pageLabel}>
                Página {page + 1} de {pageData.totalPages}
              </span>
              <button
                type="button"
                className={styles.navButton}
                disabled={page >= pageData.totalPages - 1}
                onClick={() => setPage((current) => current + 1)}
              >
                seguintes →
              </button>
            </div>
          )}
        </>
      )}

      {editState && canManage && (
        <UserFormModal
          key={editState.mode === 'edit' ? editState.user?.id : 'create'}
          user={editState.mode === 'edit' ? editState.user : undefined}
          teams={teams ?? []}
          roles={roles?.map((role) => role.name) ?? []}
          onClose={() => setEditState(null)}
        />
      )}
    </div>
  );
}

interface UserFormModalProps {
  user?: User;
  teams: { id: string; name: string }[];
  roles: string[];
  onClose: () => void;
}

function UserFormModal({ user, teams, roles, onClose }: UserFormModalProps) {
  const isEditing = user !== undefined;
  const createMutation = useCreateUser();
  const updateRolesMutation = useUpdateUserRoles();
  const updateTeamMutation = useUpdateUserTeam();
  const updateMinutesMutation = useUpdateUserDailyWorkMinutes();

  const [fullName, setFullName] = useState(user?.fullName ?? '');
  const [username, setUsername] = useState(user?.username ?? '');
  const [email, setEmail] = useState(user?.email ?? '');
  const [password, setPassword] = useState('');
  const [teamId, setTeamId] = useState(user?.teamId ?? '');
  const [selectedRoles, setSelectedRoles] = useState<string[]>(user ? [...user.roles] : ['EMPLOYEE']);
  const [dailyWorkMinutes, setDailyWorkMinutes] = useState(user?.dailyWorkMinutes ?? 480);
  const [error, setError] = useState<string | null>(null);

  const isSubmitting =
    createMutation.isPending ||
    updateRolesMutation.isPending ||
    updateTeamMutation.isPending ||
    updateMinutesMutation.isPending;

  function toggleRole(name: string) {
    setSelectedRoles((current) =>
      current.includes(name) ? current.filter((role) => role !== name) : [...current, name],
    );
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      if (isEditing && user) {
        const updates: Promise<unknown>[] = [];
        if (JSON.stringify([...selectedRoles].sort()) !== JSON.stringify([...user.roles].sort())) {
          updates.push(updateRolesMutation.mutateAsync({ id: user.id, roles: selectedRoles }));
        }
        if ((teamId || null) !== user.teamId) {
          updates.push(updateTeamMutation.mutateAsync({ id: user.id, teamId: teamId || null }));
        }
        if (dailyWorkMinutes !== user.dailyWorkMinutes) {
          updates.push(updateMinutesMutation.mutateAsync({ id: user.id, dailyWorkMinutes }));
        }
        await Promise.all(updates);
      } else {
        await createMutation.mutateAsync({
          fullName,
          username,
          email,
          password,
          teamId: teamId || null,
        });
      }
      onClose();
    } catch (err) {
      setError(err instanceof ApiError && err.problem.detail ? err.problem.detail : 'Não foi possível guardar o utilizador.');
    }
  }

  return (
    <Modal title={isEditing ? `Editar ${user?.fullName}` : 'Novo utilizador'} onClose={onClose}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <Field
          label="Nome completo"
          name="fullName"
          value={fullName}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setFullName(event.target.value)}
          required
        />
        <Field
          label="Utilizador"
          name="username"
          value={username}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setUsername(event.target.value)}
          required
          disabled={isEditing}
        />
        <Field
          label="Email"
          name="email"
          type="email"
          value={email}
          onChange={(event: ChangeEvent<HTMLInputElement>) => setEmail(event.target.value)}
          required
        />
        {!isEditing && (
          <Field
            label="Palavra-passe"
            name="password"
            type="password"
            value={password}
            minLength={8}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setPassword(event.target.value)}
            required
          />
        )}

        <div className={styles.field}>
          <label className={styles.label} htmlFor="user-team">
            Departamento
          </label>
          <select
            id="user-team"
            className={styles.select}
            value={teamId}
            onChange={(event: ChangeEvent<HTMLSelectElement>) => setTeamId(event.target.value)}
          >
            <option value="">— Sem departamento —</option>
            {teams.map((team) => (
              <option key={team.id} value={team.id}>
                {team.name}
              </option>
            ))}
          </select>
        </div>

        {isEditing && (
          <div className={styles.field}>
            <span className={styles.label}>Perfis de acesso</span>
            <div className={styles.roleOptions}>
              {roles.map((role) => (
                <label key={role} className={styles.roleOption}>
                  <input
                    type="checkbox"
                    checked={selectedRoles.includes(role)}
                    onChange={() => toggleRole(role)}
                  />
                  {role}
                </label>
              ))}
            </div>
          </div>
        )}

        <div className={styles.field}>
          <label className={styles.label} htmlFor="user-minutes">
            Horas diárias
          </label>
          <input
            id="user-minutes"
            type="number"
            min={1}
            max={1440}
            className={styles.input}
            value={dailyWorkMinutes}
            onChange={(event: ChangeEvent<HTMLInputElement>) => setDailyWorkMinutes(Number(event.target.value))}
          />
        </div>

        {error && <span className={styles.error}>{error}</span>}

        <div className={styles.actions}>
          <Button type="button" className={styles.secondaryButton} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isEditing ? 'Guardar' : 'Criar'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}

export default UsersPage;