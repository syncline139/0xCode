# Spring JWT Frontend

React + TypeScript + Vite frontend для Spring Boot backend с JWT авторизацией.

## Установка

```bash
npm install
```

## Запуск

Создайте `.env` по примеру `.env.example`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Запустите frontend:

```bash
npm run dev
```

## Backend endpoints

- `POST /api/auth/sign-up` - регистрация
- `POST /api/auth/verify` - подтверждение аккаунта
- `POST /api/auth/refreshVerifyCode` - повторная отправка кода
- `POST /api/auth/sign-in` - вход, возвращает `accessToken`
- `POST /api/auth/refresh` - refresh access token через HttpOnly cookie
- `POST /api/auth/logout` - logout
- `GET /api/users/me` - текущий пользователь

## Токены

`refreshToken` хранится backend-ом в HttpOnly cookie `refreshToken` с path `/api/auth`.
Frontend не читает refresh token. Браузер отправляет cookie автоматически для `/api/auth/refresh`, потому Axios instance использует `withCredentials: true`.

`accessToken` хранится только в памяти приложения через `tokenManager.ts`; `localStorage` и `sessionStorage` не используются.
