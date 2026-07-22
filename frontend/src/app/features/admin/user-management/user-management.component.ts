import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AdminUserApiService } from '../../../core/api/admin-user-api.service';
import { UserAdminResponse } from '../../../shared/models/admin.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge.component';
import { UserEditDialogComponent } from './user-edit-dialog/user-edit-dialog.component';

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
    StatusBadgeComponent
  ],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  private readonly adminUserApi = inject(AdminUserApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly displayedColumns: string[] = ['username', 'role', 'active', 'mustChangePassword', 'actions'];
  readonly dataSource = new MatTableDataSource<UserAdminResponse>([]);

  ngOnInit(): void {
    this.loadUsers();
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
}
