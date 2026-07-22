import { Injectable } from '@angular/core';
import { ApiClient } from './api-client.service';
import { UserAdminResponse, CreateUserRequest, UpdateUserRequest, AdminResetPasswordRequest } from '../../shared/models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminUserApiService extends ApiClient {
  listUsers() {
    return this.get<UserAdminResponse[]>('/admin/users');
  }

  createUser(request: CreateUserRequest) {
    return this.post<UserAdminResponse>('/admin/users', request);
  }

  updateUser(userId: string, request: UpdateUserRequest) {
    return this.put<UserAdminResponse>(`/admin/users/${userId}`, request);
  }

  resetPassword(userId: string, request: AdminResetPasswordRequest) {
    return this.put<void>(`/admin/users/${userId}/password`, request);
  }
}
