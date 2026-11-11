import type { PalabraFV } from './evaluacionService';
export function recuperarPalabras(palabras: PalabraFV[]): string[] {
  const recuperadas: string[] = [];
  palabras.forEach(({ ordenEntrada, texto }) => {
    recuperadas[ordenEntrada - 1] = texto;
  });
  return recuperadas;
}
