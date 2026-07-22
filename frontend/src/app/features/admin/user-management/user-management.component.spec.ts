import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { UserManagementComponent } from './user-management.component';
import { AdminUserApiService } from '../../../core/api/admin-user-api.service';
import { WorkspaceRole } from '../../../shared/models/admin.model';

describe('UserManagementComponent', () => {
  let component: UserManagementComponent;
  let fixture: ComponentFixture<UserManagementComponent>;
  let adminUserApiSpy: any;

  beforeEach(async () => {
    adminUserApiSpy = {
      listUsers: vi.fn().mockReturnValue(of([
        { id: '1', username: 'admin', role: WorkspaceRole.ADMIN, active: true, mustChangePassword: false },
        { id: '2', username: 'user', role: WorkspaceRole.USER, active: true, mustChangePassword: true }
      ]))
    };

    await TestBed.configureTestingModule({
      imports: [UserManagementComponent, NoopAnimationsModule],
      providers: [
        { provide: AdminUserApiService, useValue: adminUserApiSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load users on init', () => {
    expect(adminUserApiSpy.listUsers).toHaveBeenCalled();
    expect(component.dataSource.data.length).toBe(2);
    expect(component.dataSource.data[0].username).toBe('admin');
  });
});
