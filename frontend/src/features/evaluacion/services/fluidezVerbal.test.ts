import { describe, expect, it } from 'vitest';
import { recuperarPalabras } from './fluidezVerbal';

describe('recuperar palabras de E6', () => {
  it('respeta el ordenEntrada aunque lleguen desordenadas y conserva huecos', () => {
    const palabras = recuperarPalabras([
      { ordenEntrada: 3, texto: ' montaña ' },
      { ordenEntrada: 1, texto: 'mesa' },
      { ordenEntrada: 75, texto: 'última' },
    ]);
    expect(palabras[0]).toBe('mesa');
    expect(palabras[1]).toBeUndefined();
    expect(palabras[2]).toBe(' montaña ');
    expect(palabras[74]).toBe('última');
  });
});
