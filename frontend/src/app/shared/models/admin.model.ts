export enum WorkspaceRole {
  USER = 'USER',
  ADMIN = 'ADMIN'
}

export interface UserAdminResponse {
  id: string;
  username: string;
  active: boolean;
  role: WorkspaceRole;
  mustChangePassword: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  username: string;
  password?: string;
  role: WorkspaceRole;
}

export interface UpdateUserRequest {
  active: boolean;
  role: WorkspaceRole;
  mustChangePassword: boolean;
}

export interface AdminResetPasswordRequest {
  newPassword: string;
}
