import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AdminUserApiService } from '../../../../core/api/admin-user-api.service';
import { UserAdminResponse, WorkspaceRole } from '../../../../shared/models/admin.model';
import { AuthService } from '../../../../core/auth/auth.service';

export interface UserEditDialogData {
  mode: 'create' | 'edit' | 'reset-password';
  user?: UserAdminResponse;
}

@Component({
  selector: 'app-user-edit-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatIconModule
  ],
  templateUrl: './user-edit-dialog.component.html',
  styleUrl: './user-edit-dialog.component.scss'
})
export class UserEditDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly adminUserApi = inject(AdminUserApiService);
  private readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  userForm!: FormGroup;
  hidePassword = true;
  isLastAdmin = false;

  constructor(
    public dialogRef: MatDialogRef<UserEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: UserEditDialogData
  ) {}

  ngOnInit(): void {
    this.initForm();
    if (this.data.mode === 'edit' && this.data.user) {
      this.checkLastAdmin();
    }
  }

  private initForm(): void {
    const user = this.data.user;
    
    this.userForm = this.fb.group({
      username: [user?.username || '', [Validators.required, Validators.maxLength(100)]],
      role: [user?.role || WorkspaceRole.USER, [Validators.required]],
      active: [user?.active ?? true, [Validators.required]],
      mustChangePassword: [user?.mustChangePassword ?? false, [Validators.required]],
      password: ['', this.data.mode === 'edit' ? [] : [Validators.required, Validators.minLength(8), Validators.maxLength(100)]]
    });

    if (this.data.mode === 'reset-password') {
      this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(8)]);
    }
  }

  private checkLastAdmin(): void {
    // Simple frontend-side check: if current user is an admin and editing themselves, 
    // they might be the last one. The backend will enforce this anyway.
    // For a better UX, we could pass a flag from the parent component or fetch it.
    // In this v1, we just warn if the user is the current user.
    if (this.data.user?.id === this.authService.currentUser()?.userId) {
      this.isLastAdmin = true; 
    }
  }

  onSave(): void {
    if (this.userForm.invalid) return;

    const val = this.userForm.value;
    const userId = this.data.user?.id;

    if (this.data.mode === 'create') {
      this.adminUserApi.createUser({
        username: val.username,
        password: val.password,
        role: val.role
      }).subscribe({
        next: () => this.onSuccess('Benutzer erfolgreich angelegt.'),
        error: (err) => this.onError(err)
      });
    } else if (this.data.mode === 'edit' && userId) {
      this.adminUserApi.updateUser(userId, {
        active: val.active,
        role: val.role,
        mustChangePassword: val.mustChangePassword
      }).subscribe({
        next: () => this.onSuccess('Benutzer erfolgreich aktualisiert.'),
        error: (err) => this.onError(err)
      });
    } else if (this.data.mode === 'reset-password' && userId) {
      this.adminUserApi.resetPassword(userId, {
        newPassword: val.password
      }).subscribe({
        next: () => this.onSuccess('Passwort erfolgreich zurückgesetzt.'),
        error: (err) => this.onError(err)
      });
    }
  }

  private onSuccess(msg: string): void {
    this.snackBar.open(msg, 'Schließen', { duration: 3000 });
    this.dialogRef.close(true);
  }

  private onError(err: unknown): void {
    let msg = 'Ein Fehler ist aufgetreten.';
    if (err && typeof err === 'object' && 'error' in err) {
      const errorBody = (err as any).error;
      msg = errorBody?.message || msg;
    }
    this.snackBar.open(msg, 'Schließen', { duration: 5000 });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
