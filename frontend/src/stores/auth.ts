import { create } from 'zustand';

export interface AuthUser {
  id: string;
  username: string;
  email: string;
  phone: string;
  roles: string[];
  fullName: string;
}

interface AuthState {
  accessToken: string | null;
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (accessToken: string, user: AuthUser) => void;
  logout: () => void;
  setAccessToken: (token: string) => void;
  initialize: () => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  isAuthenticated: false,
  login: (accessToken, user) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('access_token', accessToken);
      localStorage.setItem('auth_user', JSON.stringify(user));
    }
    set({
      accessToken,
      user,
      isAuthenticated: true,
    });
  },
  logout: () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('access_token');
      localStorage.removeItem('auth_user');
    }
    set({
      accessToken: null,
      user: null,
      isAuthenticated: false,
    });
  },
  setAccessToken: (accessToken) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('access_token', accessToken);
    }
    set({
      accessToken,
    });
  },
  initialize: () => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('access_token');
      const userStr = localStorage.getItem('auth_user');
      if (token && userStr) {
        try {
          const user = JSON.parse(userStr);
          set({ accessToken: token, user, isAuthenticated: true });
        } catch (e) {
          localStorage.removeItem('access_token');
          localStorage.removeItem('auth_user');
        }
      }
    }
  },
  clearAuth: () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('access_token');
      localStorage.removeItem('auth_user');
    }
    set({
      accessToken: null,
      user: null,
      isAuthenticated: false,
    });
  },
}));
