import { afterEach, describe, expect, it, vi } from 'vitest';
import { evaluacionService } from './evaluacionService';

const mockFetchResponse = (status: number, body?: unknown) =>
  vi.stubGlobal(
    'fetch',
    vi
      .fn()
      .mockResolvedValue(
        new Response(body === undefined ? null : JSON.stringify(body), { status }),
      ),
  );

afterEach(() => {
  vi.unstubAllGlobals();
});

describe('evaluacionService', () => {
  it('consultarEvaluacion (E4) realiza GET a /api/v1/evaluaciones/{id}', async () => {
    const mockData = {
      id: 'EV-01',
      sujetoId: 'S-01',
      estado: 'EN_PROGRESO',
      subpruebas: [],
    };
    mockFetchResponse(200, mockData);

    const res = await evaluacionService.consultarEvaluacion('EV-01');

    expect(fetch).toHaveBeenCalledWith('/api/v1/evaluaciones/EV-01', expect.anything());
    expect(res).toEqual(mockData);
  });

  it('iniciarSubprueba (E5) realiza POST a /api/v1/evaluaciones/{id}/subpruebas/{codigo}/iniciar', async () => {
    const mockSubprueba = {
      codigo: 'VOC1',
      nombre: 'Vocabulario 1',
      orden: 1,
      duracionSegundos: 300,
      estado: 'ACTIVA',
      segundosRestantes: 300,
      inicio: '2026-11-06T10:00:00Z',
      finLimite: '2026-11-06T10:05:00Z',
      instrucciones: 'Instrucciones',
      permitirRetroceso: true,
    };
    mockFetchResponse(200, mockSubprueba);

    const res = await evaluacionService.iniciarSubprueba('EV-01', 'VOC1');

    expect(fetch).toHaveBeenCalledWith(
      '/api/v1/evaluaciones/EV-01/subpruebas/VOC1/iniciar',
      expect.objectContaining({ method: 'POST' }),
    );
    expect(res).toEqual(mockSubprueba);
  });

  it('consultarPreguntas (E6) realiza GET a /api/v1/evaluaciones/{id}/subpruebas/{codigo}/preguntas', async () => {
    const mockPreguntas = {
      codigo: 'VOC1',
      segundosRestantes: 290,
      permitirRetroceso: true,
      preguntas: [
        {
          id: 'P-01',
          numero: 1,
          enunciado: '¿Pregunta 1?',
          tipoRespuesta: 'SELECCION' as const,
          recursoMultimedia: null,
          opciones: [{ id: 'OPT-A', letra: 'A', etiqueta: 'A', texto: 'Opción A' }],
          respuestaActual: 'A',
        },
      ],
    };
    mockFetchResponse(200, mockPreguntas);

    const res = await evaluacionService.consultarPreguntas('EV-01', 'VOC1');

    expect(fetch).toHaveBeenCalledWith(
      '/api/v1/evaluaciones/EV-01/subpruebas/VOC1/preguntas',
      expect.anything(),
    );
    expect(res.preguntas).toHaveLength(1);
    expect(res.segundosRestantes).toBe(290);
  });

  it('registrarRespuesta (E7) realiza PUT con {"respuestas": [...]} (BFA §5.3)', async () => {
    const mockAvance = {
      evaluacionId: 'EV-01',
      subpruebaCodigo: 'VOC1',
      preguntaId: 'P-01',
      totalRespondidas: 1,
      totalPreguntas: 10,
      guardado: true,
    };
    mockFetchResponse(200, mockAvance);

    const requestBody = {
      preguntaId: 'P-01',
      opcion: 'A',
    };

    const res = await evaluacionService.registrarRespuesta('EV-01', 'VOC1', requestBody);

    expect(fetch).toHaveBeenCalledWith(
      '/api/v1/evaluaciones/EV-01/subpruebas/VOC1/respuestas',
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({
          respuestas: [
            {
              preguntaId: 'P-01',
              opcion: 'A',
              valorTexto: null,
              ordenEntrada: 1,
            },
          ],
        }),
      }),
    );
    expect(res.guardado).toBe(true);
    expect(res.totalRespondidas).toBe(1);
  });
});
