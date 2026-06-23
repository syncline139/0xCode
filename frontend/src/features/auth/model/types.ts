export type SignUpRequest = {
  email: string;
  password: string;
};

export type VerifyRequest = {
  email: string;
  code: string;
};

export type RefreshVerifyCodeRequest = {
  email: string;
  password: string;
};

export type SignInRequest = {
  email: string;
  password: string;
};

export type CurrentUser = {
  id?: number;
  email: string;
  role?: string;
  [key: string]: unknown;
};
