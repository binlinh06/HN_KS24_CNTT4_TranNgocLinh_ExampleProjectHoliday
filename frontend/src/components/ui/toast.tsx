import toast from 'react-hot-toast';

/**
 * Unified toast wrapper for react-hot-toast.
 * All toast calls in the app should use these helpers.
 */
export const showToast = {
  success: (message: string) => toast.success(message),
  error: (message: string) => toast.error(message),
  loading: (message: string) => toast.loading(message),
  dismiss: (toastId?: string) => toast.dismiss(toastId),
  custom: (message: string) =>
    toast(message, {
      icon: 'ℹ️',
      style: {
        background: '#EFF6FF',
        color: '#1E3A5F',
        border: '1px solid #BFDBFE',
      },
    }),
};
