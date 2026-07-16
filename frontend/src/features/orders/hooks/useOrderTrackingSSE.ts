import { useEffect, useRef, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';
import { orderKeys } from '../api';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

export const useOrderTrackingSSE = (orderId: string) => {
  const queryClient = useQueryClient();
  const [isConnected, setIsConnected] = useState(false);
  const [reconnectCount, setReconnectCount] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const ctrlRef = useRef<AbortController | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const maxReconnectAttempts = 5;
  const initialBackoffMs = 1000;

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
      console.error('SSE: Failed to refresh token', err);
      useAuthStore.getState().clearAuth(); // Log out if refresh fails
      return null;
    }
  };

  const connect = async () => {
    // If we've exceeded max reconnects, stop and report error
    if (reconnectCount >= maxReconnectAttempts) {
      setError('Kết nối thời gian thực bị gián đoạn. Vui lòng tải lại trang.');
      setIsConnected(false);
      return;
    }

    // Cancel any existing subscription
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

    const url = `${API_URL}/orders/${orderId}/events`;

    try {
      await fetchEventSource(url, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        signal: controller.signal,
        openWhenHidden: true, // keep connection alive in background tab if allowed
        async onopen(response) {
          if (response.status === 401) {
            // Close subscription
            controller.abort();
            setIsConnected(false);

            // Try refreshing token and reconnecting
            console.log('SSE: Received 401, attempting token refresh...');
            const newToken = await refreshAccessToken();
            if (newToken) {
              // Retry connect with exponential backoff
              retryConnect();
            } else {
              setError('Phiên làm việc hết hạn. Vui lòng đăng nhập lại.');
            }
            return;
          }

          if (response.ok) {
            setIsConnected(true);
            setError(null);
            setReconnectCount(0); // reset reconnect count on successful connection
            console.log('SSE: Connected successfully');
          } else {
            console.warn(`SSE: Connection error status ${response.status}`);
            retryConnect();
          }
        },
        onmessage(msg) {
          if (msg.event === 'heartbeat') {
            // Heartbeat received, connection is healthy
            return;
          }

          if (msg.event === 'tracking_update') {
            console.log('SSE: Received tracking update', msg.data);
            try {
              const data = JSON.parse(msg.data);

              // Update TanStack query cache directly or trigger invalidation
              queryClient.setQueryData(orderKeys.tracking(orderId), (oldData: any) => {
                if (!oldData) return data;
                // SSE only transmits the new status history.
                // We should append the new transition to the timeline and update currentStatus.
                const timeline = [...oldData.timeline];
                const exists = timeline.some((h: any) => h.changedAt === data.changedAt && h.status === data.status);
                if (!exists) {
                  timeline.push(data);
                }
                return {
                  ...oldData,
                  currentStatus: data.status,
                  statusUpdatedAt: data.changedAt,
                  timeline,
                };
              });

              // Also invalidate order details to update UI components
              queryClient.invalidateQueries({ queryKey: orderKeys.details(orderId) });
              queryClient.invalidateQueries({ queryKey: orderKeys.history({}) });
            } catch (err) {
              console.error('SSE: Error parsing tracking update', err);
            }
          }
        },
        onclose() {
          console.log('SSE: Connection closed by server');
          setIsConnected(false);
          retryConnect();
        },
        onerror(err) {
          console.error('SSE: Connection error', err);
          setIsConnected(false);
          retryConnect();
        }
      });
    } catch (err: any) {
      if (err.name === 'AbortError') {
        console.log('SSE: Connection aborted');
      } else {
        console.error('SSE: fetchEventSource error', err);
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
      console.log(`SSE: Reconnecting in ${backoffMs}ms (attempt ${reconnectCount + 1}/${maxReconnectAttempts})...`);

      reconnectTimeoutRef.current = setTimeout(() => {
        setReconnectCount(prev => prev + 1);
        connect();

        // Refetch REST tracking data as fallback on reconnect to avoid missed updates
        queryClient.invalidateQueries({ queryKey: orderKeys.tracking(orderId) });
        queryClient.invalidateQueries({ queryKey: orderKeys.details(orderId) });
      }, backoffMs);
    }
  };

  useEffect(() => {
    if (orderId) {
      connect();
    }

    return () => {
      if (ctrlRef.current) {
        ctrlRef.current.abort();
      }
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orderId]);

  return { isConnected, error };
};
