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
  id: string;
  etiqueta: string;
  texto: string;
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

export interface RegistrarRespuestaRequest {
  preguntaId: string;
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
    return http<AvanceSubpruebaResponse>(
      `/evaluaciones/${encodeURIComponent(evaluacionId)}/subpruebas/${encodeURIComponent(subpruebaCodigo)}/respuestas`,
      {
        method: 'PUT',
        body: JSON.stringify(body),
      },
    );
  },
};

