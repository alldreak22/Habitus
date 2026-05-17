import profileMock from '../content/profileMock.json';

const PROFILE_STORAGE_KEY = 'habitus-profile-overview';
export const PROFILE_UPDATED_EVENT = 'habitus-profile-updated';

export async function getProfileOverview() {
  const storedProfile = window.localStorage.getItem(PROFILE_STORAGE_KEY);

  if (!storedProfile) {
    return profileMock;
  }

  return {
    ...profileMock,
    profile: {
      ...profileMock.profile,
      ...JSON.parse(storedProfile),
    },
  };
}

export async function saveProfile(profile) {
  window.localStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(profile));
  window.dispatchEvent(new CustomEvent(PROFILE_UPDATED_EVENT, { detail: profile }));

  return profile;
}
