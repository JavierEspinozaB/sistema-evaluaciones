import { describe, expect, it, vi } from 'vitest';
import { GuardadoRespuestas } from './guardadoRespuestas';

describe('guardado de cambios durante peticiones en curso', () => {
  it('guarda el último cambio, conserva otras entradas y omite clics duplicados', async () => {
    let terminar!: () => void;
    const primera = new Promise<void>((resolve) => {
      terminar = resolve;
    });
    const estado = vi.fn();
    const guardarA = vi.fn(() => primera);
    const guardarB = vi.fn(async () => undefined);
    const guardarOtra = vi.fn(async () => undefined);
    const cola = new GuardadoRespuestas(estado, vi.fn());
    cola.enviar('1', 'm', guardarA);
    cola.enviar('1', 'm', guardarA);
    cola.enviar('1', 'mesa', guardarB);
    cola.enviar('2', 'mano', guardarOtra);
    expect(estado).toHaveBeenLastCalledWith('saving');
    terminar();
    await vi.waitFor(() => expect(estado).toHaveBeenLastCalledWith('saved'));
    expect(guardarA).toHaveBeenCalledTimes(1);
    expect(guardarB).toHaveBeenCalledTimes(1);
    expect(guardarOtra).toHaveBeenCalledTimes(1);
    cola.enviar('1', 'mesa', guardarB);
    expect(guardarB).toHaveBeenCalledTimes(1);
  });

  it('no muestra guardado ante fallos y permite reintentar sin perder palabras', async () => {
    const guardar = vi
      .fn()
      .mockRejectedValueOnce(new Error('sin conexión'))
      .mockResolvedValueOnce(undefined);
    const estado = vi.fn();
    const error = vi.fn();
    const cola = new GuardadoRespuestas(estado, error);
    cola.enviar('75', 'mesa', guardar);
    await vi.waitFor(() => expect(estado).toHaveBeenLastCalledWith('error'));
    expect(error).toHaveBeenCalledTimes(1);
    await cola.reintentar();
    expect(guardar).toHaveBeenCalledTimes(2);
    expect(estado).toHaveBeenLastCalledWith('saved');
  });
});
