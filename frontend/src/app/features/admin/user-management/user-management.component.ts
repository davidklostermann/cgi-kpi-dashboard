import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';

import { AdminUserApiService } from '../../../core/api/admin-user-api.service';
import { UserAdminResponse } from '../../../shared/models/admin.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge.component';
import { UserEditDialogComponent } from './user-edit-dialog/user-edit-dialog.component';
import { UserDeleteDialogComponent } from './user-delete-dialog/user-delete-dialog.component';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDialogModule,
    MatSnackBarModule,
    MatDividerModule,
    StatusBadgeComponent
  ],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  private readonly adminUserApi = inject(AdminUserApiService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly displayedColumns: string[] = ['username', 'role', 'active', 'mustChangePassword', 'actions'];
  readonly dataSource = new MatTableDataSource<UserAdminResponse>([]);

  ngOnInit(): void {
    this.loadUsers();
  }

  isCurrentUser(user: UserAdminResponse): boolean {
    return user.id === this.authService.currentUser()?.userId;
  }

  canDelete(user: UserAdminResponse): boolean {
    if (this.isCurrentUser(user)) {
      return false;
    }
    // Last admin protection is primarily enforced by backend, 
    // but we can disable the button if it's the only admin in the table.
    if (user.role === 'ADMIN') {
      const admins = this.dataSource.data.filter(u => u.role === 'ADMIN' && u.active);
      if (admins.length <= 1) {
        return false;
      }
    }
    return true;
  }

  loadUsers(): void {
    this.adminUserApi.listUsers().subscribe({
      next: (users) => {
        this.dataSource.data = users;
      },
      error: () => {
        this.snackBar.open('Fehler beim Laden der Benutzer.', 'Schließen', { duration: 3000 });
      }
    });
  }

  onCreateUser(): void {
    const dialogRef = this.dialog.open(UserEditDialogComponent, {
      width: '400px',
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
      }
    });
  }

  onEditUser(user: UserAdminResponse): void {
    const dialogRef = this.dialog.open(UserEditDialogComponent, {
      width: '400px',
      data: { mode: 'edit', user }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
      }
    });
  }

  onResetPassword(user: UserAdminResponse): void {
    const dialogRef = this.dialog.open(UserEditDialogComponent, {
      width: '400px',
      data: { mode: 'reset-password', user }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
      }
    });
  }

  onDeleteUser(user: UserAdminResponse): void {
    const dialogRef = this.dialog.open(UserDeleteDialogComponent, {
      width: '400px',
      data: { user }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.adminUserApi.deleteUser(user.id).subscribe({
          next: () => {
            this.snackBar.open('Benutzer erfolgreich gelöscht.', 'Schließen', { duration: 3000 });
            this.loadUsers();
          },
          error: (err) => {
            let msg = 'Fehler beim Löschen des Benutzers.';
            if (err?.error?.message) {
              msg = err.error.message;
            }
            this.snackBar.open(msg, 'Schließen', { duration: 5000 });
          }
        });
      }
    });
  }
}
