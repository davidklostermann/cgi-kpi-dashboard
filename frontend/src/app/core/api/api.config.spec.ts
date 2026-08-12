import { describe, expect, it } from 'vitest';

import { API_BASE_URL } from './api.config';
import { environment } from '../../../environments/environment';

describe('api.config', () => {
  it('should resolve API base URL from environment (AD-8)', () => {
    expect(API_BASE_URL).toBe(environment.apiBaseUrl);
    expect(API_BASE_URL).toBe('/api');
  });
});
