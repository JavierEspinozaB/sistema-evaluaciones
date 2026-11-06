import { useCallback, useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router';
import { TestScreen } from '../../../components/TestScreen';
import type { SaveState } from '../../../components/SaveStatus';
import { ApiError } from '../../../services/http';
import {
  evaluacionService,
  type PreguntaInfo,
  type SubpruebaInfo,
} from '../services/evaluacionService';

export interface SubpruebaRunnerProps {
  evaluacionId?: string;
  subpruebaCodigo?: string;
}

export function SubpruebaRunner(props: SubpruebaRunnerProps) {
  const params = useParams<{ evaluacionId: string; codigo: string }>();
  const evaluacionId = props.evaluacionId ?? params.evaluacionId ?? '';
  const codigo = props.subpruebaCodigo ?? params.codigo ?? '';

  const [subprueba, setSubprueba] = useState<SubpruebaInfo | null>(null);
  const [preguntas, setPreguntas] = useState<PreguntaInfo[]>([]);
  const [indicePregunta, setIndicePregunta] = useState(0);
  const [respuestas, setRespuestas] = useState<Record<string, string>>({});
  const [tiempoRestante, setTiempoRestante] = useState<number | null>(null);
  const [fase, setFase] = useState<'instructions' | 'practice' | 'active' | 'closed'>(
    'instructions',
  );
  const [saveState, setSaveState] = useState<SaveState>('saved');
  const [mensajeError, setMensajeError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(true);

  // Carga inicial
  useEffect(() => {
    let montado = true;

    async function cargar() {
      if (!evaluacionId || !codigo) return;
      setCargando(true);
      setMensajeError(null);

      try {
        const evaluacion = await evaluacionService.consultarEvaluacion(evaluacionId);
        if (!montado) return;

        const sub = evaluacion.subpruebas.find(
          (s) => s.codigo.toLowerCase() === codigo.toLowerCase(),
        );

        if (!sub) {
          setMensajeError(`Subprueba ${codigo} no encontrada en la evaluación.`);
          setCargando(false);
          return;
        }

        setSubprueba(sub);

        if (sub.estado === 'CERRADA') {
          setFase('closed');
          setTiempoRestante(0);
          setCargando(false);
          return;
        }

        if (sub.estado === 'ACTIVA') {
          // Recuperar preguntas y tiempo oficial del servidor
          const res = await evaluacionService.consultarPreguntas(evaluacionId, codigo);
          if (!montado) return;

          setPreguntas(res.preguntas);
          setTiempoRestante(res.segundosRestantes ?? sub.segundosRestantes ?? 0);
          setFase('active');

          const iniciales: Record<string, string> = {};
          res.preguntas.forEach((p) => {
            if (p.respuestaActual) {
              iniciales[p.id] = p.respuestaActual;
            }
          });
          setRespuestas(iniciales);
        } else {
          // PENDIENTE: inicia en fase de instrucciones
          setFase('instructions');
          setTiempoRestante(null);
        }
      } catch (err) {
        if (!montado) return;
        const msg = err instanceof Error ? err.message : 'Error al cargar la subprueba.';
        setMensajeError(msg);
      } finally {
        if (montado) setCargando(false);
      }
    }

    cargar();

    return () => {
      montado = false;
    };
  }, [evaluacionId, codigo]);

  // Temporizador oficial del cliente (sincronizado con servidor)
  useEffect(() => {
    if (fase !== 'active' || tiempoRestante === null || tiempoRestante <= 0) return;

    const timer = setInterval(() => {
      setTiempoRestante((prev) => {
        if (prev === null || prev <= 1) {
          clearInterval(timer);
          setFase('closed');
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [fase, tiempoRestante]);

  // Iniciar tiempo oficial (llamado a E5 y luego E6)
  const iniciarTiempoOficial = useCallback(async () => {
    if (!evaluacionId || !codigo) return;
    setCargando(true);
    setMensajeError(null);

    try {
      const subIniciada = await evaluacionService.iniciarSubprueba(evaluacionId, codigo);
      setSubprueba(subIniciada);

      const res = await evaluacionService.consultarPreguntas(evaluacionId, codigo);
      setPreguntas(res.preguntas);
      setTiempoRestante(res.segundosRestantes ?? subIniciada.duracionSegundos);
      setFase('active');

      const iniciales: Record<string, string> = {};
      res.preguntas.forEach((p) => {
        if (p.respuestaActual) {
          iniciales[p.id] = p.respuestaActual;
        }
      });
      setRespuestas(iniciales);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.error?.codigo === 'SUBPRUEBA_FUERA_DE_ORDEN') {
          setMensajeError(
            'No se puede iniciar esta subprueba sin haber finalizado las subpruebas anteriores.',
          );
        } else if (err.error?.codigo === 'SUBPRUEBA_CERRADA') {
          setFase('closed');
          setMensajeError('La subprueba ya se encuentra cerrada.');
        } else {
          setMensajeError(err.message);
        }
      } else {
        setMensajeError('Error al iniciar la subprueba.');
      }
    } finally {
      setCargando(false);
    }
  }, [evaluacionId, codigo]);

  // Guardar respuesta con E7
  const enviarRespuesta = useCallback(
    async (preguntaId: string, opcionId?: string, valorTexto?: string) => {
      if (fase !== 'active' || (tiempoRestante !== null && tiempoRestante <= 0)) {
        setFase('closed');
        return;
      }

      setSaveState('saving');
      try {
        await evaluacionService.registrarRespuesta(evaluacionId, codigo, {
          preguntaId,
          opcionId: opcionId ?? null,
          valorTexto: valorTexto ?? null,
        });
        setSaveState('saved');
      } catch (err) {
        setSaveState('error');
        if (err instanceof ApiError) {
          if (
            err.error?.codigo === 'TIEMPO_AGOTADO' ||
            err.error?.codigo === 'SUBPRUEBA_NO_ACTIVA'
          ) {
            setFase('closed');
            setTiempoRestante(0);
          }
        }
      }
    },
    [evaluacionId, codigo, fase, tiempoRestante],
  );

  const guardarRespuestaRef = useRef(enviarRespuesta);
  guardarRespuestaRef.current = enviarRespuesta;

  const handleSelectOption = useCallback(
    (opcionId: string) => {
      const preguntaActual = preguntas[indicePregunta];
      if (!preguntaActual) return;

      setRespuestas((prev) => ({ ...prev, [preguntaActual.id]: opcionId }));
      guardarRespuestaRef.current(preguntaActual.id, opcionId, undefined);
    },
    [preguntas, indicePregunta],
  );

  const handleTextResponse = useCallback(
    (valor: string) => {
      const preguntaActual = preguntas[indicePregunta];
      if (!preguntaActual) return;

      setRespuestas((prev) => ({ ...prev, [preguntaActual.id]: valor }));
      guardarRespuestaRef.current(preguntaActual.id, undefined, valor);
    },
    [preguntas, indicePregunta],
  );

  const handleNext = useCallback(() => {
    if (fase === 'instructions' || fase === 'practice') {
      iniciarTiempoOficial();
      return;
    }

    if (indicePregunta < preguntas.length - 1) {
      setIndicePregunta((i) => i + 1);
    } else {
      // Última pregunta
      setFase('closed');
    }
  }, [fase, indicePregunta, preguntas.length, iniciarTiempoOficial]);

  const handlePrevious = useCallback(() => {
    if (subprueba?.permitirRetroceso && indicePregunta > 0) {
      setIndicePregunta((i) => i - 1);
    }
  }, [subprueba?.permitirRetroceso, indicePregunta]);

  if (cargando) {
    return <div className="test-screen-loading">Cargando subprueba...</div>;
  }

  if (mensajeError && fase !== 'closed') {
    return (
      <div className="test-screen-error">
        <h2>Atención</h2>
        <p>{mensajeError}</p>
      </div>
    );
  }

  const preguntaActual = preguntas[indicePregunta];
  const totalPreguntas = preguntas.length;

  return (
    <TestScreen
      testName={subprueba?.nombre ?? codigo}
      questionNumber={indicePregunta + 1}
      totalQuestions={totalPreguntas || 1}
      timeRemainingSeconds={tiempoRestante}
      phase={fase}
      phaseTitle={
        fase === 'instructions'
          ? `Instrucciones: ${subprueba?.nombre ?? codigo}`
          : fase === 'closed'
            ? 'Subprueba Finalizada'
            : undefined
      }
      phaseDescription={
        fase === 'instructions'
          ? (subprueba?.instrucciones ??
            'Lea atentamente las instrucciones antes de comenzar. El tiempo oficial comenzará una vez que presione "Comenzar subprueba".')
          : fase === 'closed'
            ? 'El tiempo para esta subprueba ha finalizado o ha completado todas las preguntas. No se admiten más respuestas.'
            : undefined
      }
      prompt={preguntaActual?.enunciado ?? ''}
      options={
        preguntaActual?.opciones.map((o) => ({
          id: o.id,
          label: o.etiqueta,
          text: o.texto,
        })) ?? []
      }
      responseType={preguntaActual?.tipoRespuesta === 'TEXTO' ? 'text' : 'selection'}
      selectedOption={preguntaActual ? (respuestas[preguntaActual.id] ?? null) : null}
      textResponse={preguntaActual ? (respuestas[preguntaActual.id] ?? '') : ''}
      saveState={saveState}
      primaryActionLabel={
        fase === 'instructions'
          ? 'Comenzar subprueba'
          : indicePregunta === totalPreguntas - 1
            ? 'Finalizar'
            : 'Siguiente'
      }
      previousActionLabel="Anterior"
      onPrevious={subprueba?.permitirRetroceso && indicePregunta > 0 ? handlePrevious : undefined}
      onSelect={handleSelectOption}
      onTextResponse={handleTextResponse}
      onNext={handleNext}
    />
  );
}
