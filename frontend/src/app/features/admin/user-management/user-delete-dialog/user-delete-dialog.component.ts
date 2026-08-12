import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { UserAdminResponse } from '../../../../shared/models/admin.model';

@Component({
  selector: 'app-user-delete-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>Benutzer löschen</h2>
    <mat-dialog-content>
      <p>Sind Sie sicher, dass Sie den Benutzer <strong>{{ data.user.username }}</strong> unwiderruflich löschen möchten?</p>
      <p class="warning-text">Diese Aktion kann nicht rückgängig gemacht werden. Alle verknüpften Daten (z.B. KI-Konfigurationen) werden ebenfalls entfernt.</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Abbrechen</button>
      <button mat-flat-button color="warn" (click)="onConfirm()">
        <mat-icon>delete</mat-icon>
        Benutzer löschen
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .warning-text {
      color: var(--cgi-error);
      font-weight: 500;
      margin-top: 1rem;
    }
  `]
})
export class UserDeleteDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<UserDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user: UserAdminResponse }
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
