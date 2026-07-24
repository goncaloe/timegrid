import { describe, expect, it } from 'vitest';
import { isJsonContentType } from './http';

describe('isJsonContentType', () => {
  it('reconhece application/json', () => {
    expect(isJsonContentType('application/json')).toBe(true);
  });

  it('reconhece application/problem+json com charset', () => {
    expect(isJsonContentType('application/problem+json;charset=UTF-8')).toBe(true);
  });

  it('reconhece outros +json', () => {
    expect(isJsonContentType('application/vnd.api+json')).toBe(true);
  });

  it('rejeita csv e pdf', () => {
    expect(isJsonContentType('text/csv;charset=UTF-8')).toBe(false);
    expect(isJsonContentType('application/pdf')).toBe(false);
  });

  it('rejeita content-type nulo ou ausente', () => {
    expect(isJsonContentType(null)).toBe(false);
    expect(isJsonContentType(undefined)).toBe(false);
  });
});