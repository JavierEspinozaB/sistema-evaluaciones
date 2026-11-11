import type { SaveState } from '../../../components/SaveStatus';

type Pendiente = { valor: string; guardar: () => Promise<unknown> };

/** Conserva el último cambio de cada entrada y permite reintentar los fallidos. */
export class GuardadoRespuestas {
  private pendientes = new Map<string, Pendiente>();
  private confirmadas = new Map<string, string>();
  private procesando = false;
  private cancelado = false;

  constructor(
    private estado: (estado: SaveState) => void,
    private error: (error: unknown) => void,
  ) {}

  enviar(clave: string, valor: string, guardar: () => Promise<unknown>) {
    if (this.cancelado) return;
    if (this.pendientes.get(clave)?.valor === valor) return;
    if (!this.pendientes.has(clave) && this.confirmadas.get(clave) === valor) return;
    this.pendientes.set(clave, { valor, guardar });
    void this.reintentar();
  }

  cancelar() {
    this.cancelado = true;
    this.pendientes.clear();
  }

  async reintentar() {
    if (this.procesando || this.cancelado || this.pendientes.size === 0) return;
    this.procesando = true;
    this.estado('saving');
    try {
      while (!this.cancelado && this.pendientes.size > 0) {
        const [clave, pendiente] = this.pendientes.entries().next().value!;
        await pendiente.guardar();
        this.confirmadas.set(clave, pendiente.valor);
        if (this.pendientes.get(clave) === pendiente) this.pendientes.delete(clave);
      }
      if (!this.cancelado) this.estado('saved');
    } catch (error) {
      if (!this.cancelado) {
        this.estado('error');
        this.error(error);
      }
    } finally {
      this.procesando = false;
    }
  }
}
