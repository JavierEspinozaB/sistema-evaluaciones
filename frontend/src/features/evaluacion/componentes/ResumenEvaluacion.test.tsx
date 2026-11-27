import { renderToStaticMarkup } from 'react-dom/server';
import { describe, expect, it } from 'vitest';
import type { EvaluacionResponse, TestDetalleResponse } from '../tipos';
import { ResumenEvaluacion } from './ResumenEvaluacion';

const evaluacion: EvaluacionResponse = {
  id: 7,
  testId: 2,
  sujetoId: 3,
  periodoId: 4,
  estado: 'EN_CURSO',
  fechaInicio: '2026-11-27T12:00:00Z',
  fechaFin: null,
  subpruebaActiva: null,
  subpruebas: [{ codigo: 'S1', estado: 'PENDIENTE' }],
};

const test: TestDetalleResponse = {
  id: 1,
  codigo: 'T1',
  nombre: 'Otro test',
  versionFormularioId: 1,
  subpruebas: [
    {
      codigo: 'S1',
      nombre: 'Nombre del otro test',
      orden: 1,
      factor: 'S1',
      tiempoLimiteSegundos: 300,
      tipoRespuesta: 'SELECCION',
      opciones: 4,
      numeroPreguntas: 1,
      maximoEntradas: null,
      tienePractica: false,
    },
  ],
};

function resumen(detalle: TestDetalleResponse | null) {
  return renderToStaticMarkup(
    <ResumenEvaluacion evaluacion={evaluacion} test={detalle} periodos={[]} titulo="Resumen" />,
  );
}

describe('resumen de evaluación recuperada', () => {
  it('usa los códigos de E4 cuando la configuración pertenece a otro test', () => {
    const html = resumen(test);
    expect(html).not.toContain('Nombre del otro test');
    expect(html).not.toContain('Otro test');
    expect(html).toContain('ID 2');
    expect(html).toContain('<strong>S1</strong>');
  });

  it('usa nombres de subpruebas cuando la configuración corresponde al test', () => {
    const html = resumen({ ...test, id: 2 });
    expect(html).toContain('<strong>Nombre del otro test</strong>');
  });
});
