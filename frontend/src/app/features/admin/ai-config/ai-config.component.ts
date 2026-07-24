import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AiConfigService, AiProviderConfig, AI_CONFIG_MESSAGES } from './ai-config.service';

@Component({
  selector: 'app-ai-config',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  templateUrl: './ai-config.component.html',
  styleUrls: ['./ai-config.component.scss']
})
export class AiConfigComponent implements OnInit {
  private fb = inject(FormBuilder);
  private aiConfigService = inject(AiConfigService);
  private snackBar = inject(MatSnackBar);

  configForm: FormGroup = this.fb.group({
    provider: ['gemini', [Validators.required]],
    model: ['', [Validators.required]],
    apiKey: [''],
    enabled: [true]
  });

  isLoading = false;
  isTesting = false;
  apiKeyMasked = '';

  ngOnInit(): void {
    this.loadConfig();
  }

  loadConfig(): void {
    this.isLoading = true;
    this.aiConfigService.getConfig('gemini').subscribe({
      next: (config) => {
        if (config) {
          this.configForm.patchValue({
            provider: config.provider,
            model: config.model,
            enabled: config.enabled,
            apiKey: '' // Keep empty to not overwrite if not changed
          });
          this.apiKeyMasked = config.apiKeyMasked;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.snackBar.open(AI_CONFIG_MESSAGES.LOAD_CONFIG_ERROR, 'OK', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onSave(): void {
    if (this.configForm.invalid) return;

    this.isLoading = true;
    const formValue = this.configForm.value;
    this.aiConfigService.saveConfig(formValue).subscribe({
      next: (config) => {
        this.snackBar.open(AI_CONFIG_MESSAGES.SAVE_CONFIG_SUCCESS, 'OK', { duration: 3000 });
        this.apiKeyMasked = config.apiKeyMasked;
        this.configForm.patchValue({ apiKey: '' });
        this.isLoading = false;
      },
      error: (err) => {
        this.snackBar.open(AI_CONFIG_MESSAGES.SAVE_CONFIG_ERROR, 'OK', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onTest(): void {
    this.isTesting = true;
    this.aiConfigService.testConnection('gemini').subscribe({
      next: (res) => {
        if (res.success) {
          this.snackBar.open(AI_CONFIG_MESSAGES.CONNECTION_TEST_SUCCESS, 'OK', { duration: 5000, panelClass: ['success-snackbar'] });
        } else {
          const message = res.message.includes('Provider') ? AI_CONFIG_MESSAGES.CONNECTION_TEST_UNSUPPORTED_PROVIDER('gemini') :
                          res.message.includes('Unerwartete Antwort') ? AI_CONFIG_MESSAGES.CONNECTION_TEST_UNEXPECTED_RESPONSE(res.message.split(': ')[1]) :
                          res.message.includes('Verbindungsfehler') ? AI_CONFIG_MESSAGES.CONNECTION_TEST_ERROR(res.message.split(': ')[1]) :
                          AI_CONFIG_MESSAGES.CONNECTION_TEST_FAILED;
          this.snackBar.open(message, 'OK', { duration: 10000 });
        }
        this.isTesting = false;
      },
      error: (err) => {
        this.snackBar.open(AI_CONFIG_MESSAGES.CONNECTION_TEST_FAILED, 'OK', { duration: 5000 });
        this.isTesting = false;
      }
    });
  }
}
