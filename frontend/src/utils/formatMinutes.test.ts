import { describe, expect, it } from 'vitest';
import { formatMinutes } from './formatMinutes';

describe('formatMinutes', () => {
  it('formata minutos com horas e minutos com dois dígitos', () => {
    expect(formatMinutes(125)).toBe('2h05');
  });

  it('formata um número exato de horas', () => {
    expect(formatMinutes(120)).toBe('2h00');
  });

  it('formata menos de uma hora', () => {
    expect(formatMinutes(45)).toBe('0h45');
  });

  it('formata zero', () => {
    expect(formatMinutes(0)).toBe('0h00');
  });

  it('formata valores negativos com o sinal antes das horas', () => {
    expect(formatMinutes(-95)).toBe('-1h35');
  });
});