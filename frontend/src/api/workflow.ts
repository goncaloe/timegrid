export interface StatusChange {
  id: string;
  fromStatus: string | null;
  toStatus: string;
  changedBy: string;
  comment: string | null;
  changedAt: string;
}