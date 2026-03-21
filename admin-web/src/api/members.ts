import { request } from '@/api/http';

export interface MemberQuery {
  storeId?: number;
  countryCode?: string;
  levelCode?: string;
  keyword?: string;
}

export interface MemberSummary {
  id: number;
  storeId: number;
  storeName: string;
  levelCode: string;
  displayName: string;
  countryCode: string;
  phoneNational: string;
  phoneE164: string;
}

export interface MemberPayload {
  storeId: number;
  levelCode: string;
  displayName: string;
  countryCode: string;
  phoneNational: string;
  phoneE164: string;
}

function asRecord(value: unknown): Record<string, unknown> {
  if (!value || typeof value !== 'object') {
    return {};
  }
  return value as Record<string, unknown>;
}

function asString(value: unknown): string {
  return typeof value === 'string' ? value : '';
}

function asNumber(value: unknown): number {
  return typeof value === 'number' ? value : 0;
}

export async function fetchMembers(query: MemberQuery): Promise<MemberSummary[]> {
  const raw = await request<unknown[]>(
    '/api/v1/members',
    undefined,
    query as Record<string, string | number | boolean | null | undefined>,
  );
  return raw.map(toMemberSummary);
}

function toMemberSummary(item: unknown): MemberSummary {
  const source = asRecord(item);
  return {
    id: asNumber(source.id),
    storeId: asNumber(source.storeId),
    storeName: asString(source.storeName),
    levelCode: asString(source.levelCode),
    displayName: asString(source.displayName || source.name),
    countryCode: asString(source.countryCode),
    phoneNational: asString(source.phoneNational),
    phoneE164: asString(source.phoneE164),
  };
}

export async function createMember(payload: MemberPayload): Promise<MemberSummary> {
  const raw = await request<unknown>('/api/v1/members', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  return toMemberSummary(raw);
}

export async function updateMember(id: number, payload: MemberPayload): Promise<MemberSummary> {
  const raw = await request<unknown>(`/api/v1/members/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
  return toMemberSummary(raw);
}

export async function deleteMember(id: number): Promise<void> {
  await request<void>(`/api/v1/members/${id}`, {
    method: 'DELETE',
  });
}
