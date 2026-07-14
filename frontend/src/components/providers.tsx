'use client';

import React, { useEffect, useRef } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 5 * 60 * 1000,
    },
  },
});

function AuthInitializer({ children }: { children: React.ReactNode }) {
  const { setAuth, clearAuth, setStatus } = useAuthStore();
  const initialized = useRef(false);

  useEffect(() => {
    if (initialized.current) return;
    initialized.current = true;

    const initAuth = async () => {
      try {
        // Step 1: Try to refresh token (sends HttpOnly cookie)
        const refreshRes = await axios.post(
          `${API_URL}/auth/refresh`,
          {},
          { withCredentials: true }
        );

        const { accessToken } = refreshRes.data.data;

        // Step 2: Call /auth/me with the new access token
        const meRes = await axios.get(`${API_URL}/auth/me`, {
          headers: { Authorization: `Bearer ${accessToken}` },
          withCredentials: true,
        });

        const user = meRes.data.data;
        setAuth(user, accessToken);
      } catch {
        // No valid session — user is not authenticated
        clearAuth();
      }
    };

    initAuth();
  }, [setAuth, clearAuth, setStatus]);

  return <>{children}</>;
}

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthInitializer>
        {children}
      </AuthInitializer>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            fontFamily: 'var(--font-be-vietnam-pro), Inter, sans-serif',
            fontSize: '14px',
            borderRadius: '10px',
            padding: '12px 16px',
          },
          success: {
            style: {
              background: '#ECFDF5',
              color: '#042F22',
              border: '1px solid #D1FAE5',
            },
          },
          error: {
            style: {
              background: '#FEF2F2',
              color: '#7F1D1D',
              border: '1px solid #FECACA',
            },
          },
        }}
      />
    </QueryClientProvider>
  );
}
