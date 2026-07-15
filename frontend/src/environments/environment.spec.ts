import { describe, expect, it } from 'vitest';

import { environment } from './environment';

describe('environment', () => {
  it('should expose only apiBaseUrl without secrets (AD-8)', () => {
    expect(Object.keys(environment)).toEqual(['apiBaseUrl']);
    expect(environment.apiBaseUrl).toBe('/api');
  });
});
