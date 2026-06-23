let accessToken: string | null = null;
const listeners = new Set<(token: string | null) => void>();

function notifyListeners() {
  listeners.forEach((listener) => listener(accessToken));
}

export function getAccessToken() {
  return accessToken;
}

export function setAccessToken(token: string) {
  accessToken = token;
  notifyListeners();
}

export function clearAccessToken() {
  accessToken = null;
  notifyListeners();
}

export function subscribeAccessToken(listener: (token: string | null) => void) {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}
