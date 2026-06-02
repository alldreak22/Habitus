import { apiRequest } from './api.js';

export const PROFILE_UPDATED_EVENT = 'habitus-profile-updated';
const USER_STORAGE_KEY = 'habitus-user';
const DEFAULT_PROFILE = {
  email: '',
  createdAt: null,
  imageUrl: null,
  memberSince: '-',
  name: '',
  nickname: '',
};

export function formatMemberSince(createdAt) {
  if (!createdAt) {
    return '-';
  }

  const date = new Date(createdAt);
  if (Number.isNaN(date.getTime())) {
    return '-';
  }

  return new Intl.DateTimeFormat('pt-BR', {
    month: 'long',
    year: 'numeric',
  })
    .format(date)
    .replace(/^./, (character) => character.toUpperCase());
}

export async function getProfileOverview() {
  let cachedUser = {};
  try {
    cachedUser = JSON.parse(window.localStorage.getItem(USER_STORAGE_KEY) ?? '{}');
  } catch {
    cachedUser = {};
  }

  const apiUser = await apiRequest('/users/me');
  const apiProfile = {
    createdAt: apiUser?.createdAt ?? null,
    name: apiUser?.name ?? '',
    nickname: apiUser?.nick ?? apiUser?.name ?? '',
    email: apiUser?.email ?? '',
    imageUrl: apiUser?.picture ?? null,
  };
  const memberSince = formatMemberSince(apiProfile.createdAt ?? cachedUser.createdAt ?? null);
  const profile = {
    ...DEFAULT_PROFILE,
    ...cachedUser,
    ...apiProfile,
    memberSince,
  };
  window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(profile));

  return {
    focusPrompt: null,
    profile,
    security: null,
    summary: null,
  };
}

export async function saveProfile(profile) {
  if (!profile?.name || !profile?.email) {
    throw new Error('Nome e e-mail são obrigatórios.');
  }

  const savedProfile = await apiRequest('/users/me', {
    method: 'PUT',
    body: JSON.stringify({
      email: profile.email,
      name: profile.name,
      nick: profile.nickname,
      picture: profile.imageUrl ?? null,
    }),
  });
  const normalized = {
    ...DEFAULT_PROFILE,
    ...profile,
    createdAt: savedProfile?.createdAt ?? profile.createdAt ?? null,
    email: savedProfile?.email ?? profile.email,
    imageUrl: savedProfile?.picture ?? profile.imageUrl ?? null,
    name: savedProfile?.name ?? profile.name,
    nickname: savedProfile?.nick ?? profile.nickname ?? profile.name,
  };
  normalized.memberSince = formatMemberSince(normalized.createdAt);
  window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(normalized));
  window.dispatchEvent(new CustomEvent(PROFILE_UPDATED_EVENT, { detail: normalized }));

  return normalized;
}

export async function changePassword({ currentPassword, newPassword }) {
  return apiRequest('/users/me/password', {
    method: 'PUT',
    body: JSON.stringify({
      currentPassword,
      newPassword,
    }),
  });
}
