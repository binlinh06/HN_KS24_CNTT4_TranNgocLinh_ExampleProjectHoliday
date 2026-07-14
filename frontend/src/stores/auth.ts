import { create } from 'zustand';

export interface AuthUser {
  id: string;
  username: string;
  email: string;
  phone: string;
  roles: string[];
  fullName: string;
}

export type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

interface AuthState {
  user: AuthUser | null;
  accessToken: string | null;
  authStatus: AuthStatus;
  isAuthenticated: boolean;
  setAuth: (user: AuthUser, accessToken: string) => void;
  clearAuth: () => void;
  setStatus: (status: AuthStatus) => void;
  setAccessToken: (token: string) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  authStatus: 'loading',
  isAuthenticated: false,

  setAuth: (user, accessToken) => {
    set({ user, accessToken, authStatus: 'authenticated', isAuthenticated: true });
  },

  clearAuth: () => {
    set({ user: null, accessToken: null, authStatus: 'unauthenticated', isAuthenticated: false });
  },

  setStatus: (authStatus) => {
    set({ 
      authStatus,
      isAuthenticated: authStatus === 'authenticated'
    });
  },

  setAccessToken: (accessToken) => {
    set({ accessToken });
  },
}));
