import axios from 'axios';

type BackendErrorResponse = {
  message?: string;
};

export function getErrorMessage(error: unknown, fallback = 'Request failed') {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as unknown;

    if (typeof data === 'string' && data.trim()) {
      return data;
    }

    if (isBackendErrorResponse(data) && data.message) {
      return data.message;
    }
  }

  if (error instanceof Error) {
    return error.message;
  }

  return fallback;
}

function isBackendErrorResponse(value: unknown): value is BackendErrorResponse {
  return typeof value === 'object' && value !== null && 'message' in value;
}

export function FieldError({ message }: { message?: string }) {
  if (!message) {
    return null;
  }

  return <p className="mt-1 text-sm text-red-600">{message}</p>;
}

export function FormMessage({ type, children }: { type: 'success' | 'error'; children?: string }) {
  if (!children) {
    return null;
  }

  return (
    <div
      className={`rounded-md border px-3 py-2 text-sm ${
        type === 'success' ? 'border-emerald-200 bg-emerald-50 text-emerald-800' : 'border-red-200 bg-red-50 text-red-700'
      }`}
    >
      {children}
    </div>
  );
}
