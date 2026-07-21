import { useEffect, useRef, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';
import { staffKeys } from '../api';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

export const useStaffSSE = (channel: 'orders' | 'kitchen') => {
  const queryClient = useQueryClient();
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reconnectCount, setReconnectCount] = useState(0);

  const ctrlRef = useRef<AbortController | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const seenEventIds = useRef<Set<String>>(new Set());

  const maxReconnectAttempts = 5;
  const initialBackoffMs = 1000;

  const playSoundAlert = (type: 'new' | 'ready') => {
    try {
      const ctx = new (window.AudioContext || (window as any).webkitAudioContext)();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      
      osc.connect(gain);
      gain.connect(ctx.destination);
      
      if (type === 'new') {
        osc.frequency.setValueAtTime(523.25, ctx.currentTime); // C5
        osc.frequency.setValueAtTime(659.25, ctx.currentTime + 0.15); // E5
        gain.gain.setValueAtTime(0.1, ctx.currentTime);
        osc.start();
        osc.stop(ctx.currentTime + 0.35);
      } else {
        osc.frequency.setValueAtTime(783.99, ctx.currentTime); // G5
        osc.frequency.setValueAtTime(880.00, ctx.currentTime + 0.12); // A5
        osc.frequency.setValueAtTime(1046.50, ctx.currentTime + 0.24); // C6
        gain.gain.setValueAtTime(0.1, ctx.currentTime);
        osc.start();
        osc.stop(ctx.currentTime + 0.4);
      }
    } catch (e) {
      console.warn('AudioContext alert failed to play:', e);
    }
  };

  const refreshAccessToken = async (): Promise<string | null> => {
    try {
      const response = await axios.post(
        `${API_URL}/auth/refresh`,
        {},
        { withCredentials: true }
      );
      const { accessToken } = response.data.data;
      useAuthStore.getState().setAccessToken(accessToken);
      return accessToken;
    } catch (err) {
      console.error('Staff SSE: Failed to refresh token', err);
      useAuthStore.getState().clearAuth();
      return null;
    }
  };

  const connect = async () => {
    if (reconnectCount >= maxReconnectAttempts) {
      setError('Kết nối thời gian thực bị gián đoạn. Vui lòng tải lại trang.');
      setIsConnected(false);
      return;
    }

    if (ctrlRef.current) {
      ctrlRef.current.abort();
    }

    let token = useAuthStore.getState().accessToken;
    if (!token) {
      setError('Người dùng chưa đăng nhập');
      return;
    }

    const controller = new AbortController();
    ctrlRef.current = controller;

    const url = `${API_URL}/staff/${channel}/events`;

    try {
      await fetchEventSource(url, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        signal: controller.signal,
        openWhenHidden: true,
        async onopen(response) {
          if (response.status === 401) {
            controller.abort();
            setIsConnected(false);
            
            console.log('Staff SSE: Received 401, refreshing token...');
            const newToken = await refreshAccessToken();
            if (newToken) {
              retryConnect();
            } else {
              setError('Phiên làm việc hết hạn. Vui lòng đăng nhập lại.');
            }
            return;
          }

          if (response.ok) {
            setIsConnected(true);
            setError(null);
            setReconnectCount(0);
            console.log(`Staff SSE [${channel}]: Connected successfully`);
          } else {
            console.warn(`Staff SSE: Error status ${response.status}`);
            retryConnect();
          }
        },
        onmessage(msg) {
          if (msg.event === 'heartbeat') {
            return;
          }

          try {
            const data = JSON.parse(msg.data);
            const eventId = data.eventId;
            
            // Deduplicate events by eventId
            if (eventId && seenEventIds.current.has(eventId)) {
              return;
            }
            if (eventId) {
              seenEventIds.current.add(eventId);
            }

            console.log(`Staff SSE [${channel}] Event [${msg.event}]:`, data);

            // Audio Notifications
            if (msg.event === 'NEW_ORDER') {
              playSoundAlert('new');
            } else if (msg.event === 'ORDER_ALL_ITEMS_READY') {
              playSoundAlert('ready');
            }

            // Invalidate caches based on event types
            if (msg.event === 'KITCHEN_ITEM_CREATED' || msg.event === 'KITCHEN_ITEM_UPDATED') {
              queryClient.invalidateQueries({ queryKey: staffKeys.kitchenQueue });
            }
            
            queryClient.invalidateQueries({ queryKey: staffKeys.tables });
            queryClient.invalidateQueries({ queryKey: staffKeys.all });
          } catch (err) {
            console.error('Staff SSE: Error parsing payload', err);
          }
        },
        onclose() {
          console.log(`Staff SSE [${channel}]: Connection closed by server`);
          setIsConnected(false);
          retryConnect();
        },
        onerror(err) {
          console.error(`Staff SSE [${channel}]: Connection error`, err);
          setIsConnected(false);
          retryConnect();
        }
      });
    } catch (err: any) {
      if (err.name === 'AbortError') {
        console.log(`Staff SSE [${channel}]: Connection aborted`);
      } else {
        console.error(`Staff SSE [${channel}]: fetchEventSource error`, err);
        retryConnect();
      }
    }
  };

  const retryConnect = () => {
    setIsConnected(false);
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
    }

    if (reconnectCount < maxReconnectAttempts) {
      const backoffMs = initialBackoffMs * Math.pow(2, reconnectCount);
      console.log(`Staff SSE [${channel}]: Reconnecting in ${backoffMs}ms...`);
      
      reconnectTimeoutRef.current = setTimeout(() => {
        setReconnectCount(prev => prev + 1);
        connect();
        
        // REST refetch fallback
        queryClient.invalidateQueries({ queryKey: staffKeys.all });
      }, backoffMs);
    }
  };

  useEffect(() => {
    connect();

    return () => {
      if (ctrlRef.current) {
        ctrlRef.current.abort();
      }
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [channel]);

  return { isConnected, error };
};
