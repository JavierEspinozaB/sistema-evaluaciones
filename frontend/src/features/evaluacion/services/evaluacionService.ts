import { http } from '../../../services/http';

export interface SubpruebaInfo {
  codigo: string;
  nombre: string;
  orden: number;
  duracionSegundos: number;
  estado: 'PENDIENTE' | 'ACTIVA' | 'CERRADA';
  segundosRestantes: number | null;
  inicio: string | null;
  finLimite: string | null;
  instrucciones: string | null;
  permitirRetroceso: boolean;
}

export interface EvaluacionInfo {
  id: string;
  sujetoId: string;
  estado: string;
  subpruebas: SubpruebaInfo[];
}

export interface OpcionInfo {
  letra?: string;
  texto: string;
  id?: string;
  etiqueta?: string;
}

export interface PreguntaInfo {
  id: string;
  numero: number;
  enunciado: string;
  tipoRespuesta: 'SELECCION' | 'TEXTO';
  recursoMultimedia: string | null;
  opciones: OpcionInfo[];
  respuestaActual: string | null;
}

export interface PreguntasSubpruebaResponse {
  codigo: string;
  segundosRestantes: number | null;
  permitirRetroceso: boolean;
  preguntas: PreguntaInfo[];
}

export interface ItemRespuesta {
  preguntaId: string;
  opcion?: string | null;
  opcionId?: string | null;
  valorTexto?: string | null;
  ordenEntrada?: number;
}

export interface RegistrarRespuestaRequest {
  respuestas?: ItemRespuesta[];
  preguntaId?: string;
  opcion?: string | null;
  opcionId?: string | null;
  valorTexto?: string | null;
  ordenEntrada?: number;
}

export interface AvanceSubpruebaResponse {
  evaluacionId: string;
  subpruebaCodigo: string;
  preguntaId: string;
  totalRespondidas: number;
  totalPreguntas: number;
  guardado: boolean;
}

export const evaluacionService = {
  consultarEvaluacion(evaluacionId: string): Promise<EvaluacionInfo> {
    return http<EvaluacionInfo>(`/evaluaciones/${encodeURIComponent(evaluacionId)}`);
  },

  iniciarSubprueba(evaluacionId: string, subpruebaCodigo: string): Promise<SubpruebaInfo> {
    return http<SubpruebaInfo>(
      `/evaluaciones/${encodeURIComponent(evaluacionId)}/subpruebas/${encodeURIComponent(subpruebaCodigo)}/iniciar`,
      { method: 'POST' },
    );
  },

  consultarPreguntas(
    evaluacionId: string,
    subpruebaCodigo: string,
  ): Promise<PreguntasSubpruebaResponse> {
    return http<PreguntasSubpruebaResponse>(
      `/evaluaciones/${encodeURIComponent(evaluacionId)}/subpruebas/${encodeURIComponent(subpruebaCodigo)}/preguntas`,
    );
  },

  registrarRespuesta(
    evaluacionId: string,
    subpruebaCodigo: string,
    body: RegistrarRespuestaRequest,
  ): Promise<AvanceSubpruebaResponse> {
    const payload =
      body.respuestas != null
        ? body
        : {
            respuestas: [
              {
                preguntaId: body.preguntaId!,
                opcion: body.opcion ?? body.opcionId ?? null,
                valorTexto: body.valorTexto ?? null,
                ordenEntrada: body.ordenEntrada ?? 1,
              },
            ],
          };

    return http<AvanceSubpruebaResponse>(
      `/evaluaciones/${encodeURIComponent(evaluacionId)}/subpruebas/${encodeURIComponent(subpruebaCodigo)}/respuestas`,
      {
        method: 'PUT',
        body: JSON.stringify(payload),
      },
    );
  },
};
