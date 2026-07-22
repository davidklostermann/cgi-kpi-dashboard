/** Session user from GET /api/auth/me (Story 11.4 / 11.5). */
export interface AuthUser {
  userId: string;
  workspaceId: string;
  username: string;
  roles: string[];
  mustChangePassword: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface ApiErrorBody {
  code: string;
  message: string;
}
