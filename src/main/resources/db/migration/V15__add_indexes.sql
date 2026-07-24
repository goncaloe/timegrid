-- time_entries(user_id, work_date) já existe desde a V7; os pendentes de aprovação filtram por organização + estado
create index idx_time_entries_org_status on time_entries (organization_id, status);

-- idem para ausências: absence_requests(user_id, start_date, end_date) já cobre os pedidos próprios,
-- falta o par usado por /absences/pending
create index idx_absence_requests_org_status on absence_requests (organization_id, status);

-- contagem/lista de não lidas é o caminho mais frequente (sino da topbar, a cada 60s)
create index idx_notifications_recipient_unread on notifications (recipient_id, read_at);