import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import Spinner from './components/Spinner';
import RequireAuth from './auth/RequireAuth';
import DashboardPage from './pages/DashboardPage';
import LoginPage from './pages/LoginPage';
import NotFoundPage from './pages/NotFoundPage';
import TimeEntriesPage from './features/timeEntries/TimeEntriesPage';
import AbsencesPage from './features/absences/AbsencesPage';
import HolidaysPage from './features/holidays/HolidaysPage';
import ApprovalsPage from './features/approvals/ApprovalsPage';
import UsersPage from './features/users/UsersPage';
import TeamsPage from './features/teams/TeamsPage';
import OrganizationPage from './features/organizations/OrganizationPage';
import WebSocketProvider from './features/notifications/WebSocketProvider';
import { useAuth } from './auth/useAuth';
import styles from './App.module.css';

function App() {
  const { isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className={styles.loading}>
        <Spinner />
      </div>
    );
  }

  return (
    <BrowserRouter>
      <WebSocketProvider />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<RequireAuth />}>
          <Route element={<Layout />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/time-entries" element={<TimeEntriesPage />} />
            <Route path="/absences" element={<AbsencesPage />} />
            <Route path="/holidays" element={<HolidaysPage />} />
            <Route path="/approvals" element={<ApprovalsPage />} />
            <Route path="/users" element={<UsersPage />} />
            <Route path="/teams" element={<TeamsPage />} />
            <Route path="/organization" element={<OrganizationPage />} />
          </Route>
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;