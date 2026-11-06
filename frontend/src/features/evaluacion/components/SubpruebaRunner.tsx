import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useParams } from 'react-router';
import { TestScreen } from '../../../components/TestScreen';
import type { SaveState } from '../../../components/SaveStatus';
import { ApiError } from '../../../services/http';
import {
  evaluacionService,
  type PreguntaInfo,
  type SubpruebaInfo,
} from '../services/evaluacionService';
import { FluidezVerbal } from '../../bfa/components/FluidezVerbal';

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
  const [palabrasFV, setPalabrasFV] = useState<string[]>([]);
  const [tiempoRestante, setTiempoRestante] = useState<number | null>(null);
  const [fase, setFase] = useState<'instructions' | 'practice' | 'active' | 'closed'>(
    'instructions',
  );
  const [saveState, setSaveState] = useState<SaveState>('saved');
  const [mensajeError, setMensajeError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(true);

  // Estado de práctica
  const [practiceSelection, setPracticeSelection] = useState<string | null>(null);
  const [practiceText, setPracticeText] = useState<string>('');

  // Concurrencia e idempotencia en guardado (BUG-02)
  const peticionesEnCursoRef = useRef<Record<string, boolean>>({});
  const ultimosValoresGuardadosRef = useRef<Record<string, string>>({});

  const esFluidezVerbal = useMemo(() => {
    const cod = (subprueba?.codigo ?? codigo).toUpperCase();
    return cod === 'FV' || cod.includes('FLUIDEZ');
  }, [subprueba?.codigo, codigo]);

  const letraFV = useMemo(() => {
    const fuente = `${subprueba?.instrucciones ?? ''} ${preguntas[0]?.enunciado ?? ''}`;
    const match = fuente.match(/(?:letra|con la)\s+["']?([A-Za-z])["']?/i);
    return match ? match[1].toUpperCase() : 'M';
  }, [subprueba?.instrucciones, preguntas]);

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
          const palabrasRecuperadas: string[] = [];

          res.preguntas.forEach((p) => {
            if (p.respuestaActual) {
              iniciales[p.id] = p.respuestaActual;
              palabrasRecuperadas.push(p.respuestaActual);
            }
          });
          setRespuestas(iniciales);
          if (palabrasRecuperadas.length > 0) {
            setPalabrasFV(palabrasRecuperadas);
          }
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

  // Guardar respuesta con E7 y protección contra guardados duplicados (BUG-02)
  const enviarRespuesta = useCallback(
    async (preguntaId: string, opcion?: string, valorTexto?: string, ordenEntrada: number = 1) => {
      if (fase !== 'active' || (tiempoRestante !== null && tiempoRestante <= 0)) {
        setFase('closed');
        return;
      }

      const clave = `${preguntaId}_${ordenEntrada}`;
      const valorAEnviar = `${opcion ?? ''}__${valorTexto ?? ''}`;

      // Evitar envíos repetidos o en curso del mismo valor
      if (ultimosValoresGuardadosRef.current[clave] === valorAEnviar) {
        setSaveState('saved');
        return;
      }
      if (peticionesEnCursoRef.current[clave]) {
        return;
      }

      peticionesEnCursoRef.current[clave] = true;
      setSaveState('saving');

      try {
        await evaluacionService.registrarRespuesta(evaluacionId, codigo, {
          respuestas: [
            {
              preguntaId,
              opcion: opcion ?? null,
              valorTexto: valorTexto ?? null,
              ordenEntrada,
            },
          ],
        });
        ultimosValoresGuardadosRef.current[clave] = valorAEnviar;
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
      } finally {
        peticionesEnCursoRef.current[clave] = false;
      }
    },
    [evaluacionId, codigo, fase, tiempoRestante],
  );

  const guardarRespuestaRef = useRef(enviarRespuesta);
  guardarRespuestaRef.current = enviarRespuesta;

  const handleSelectOption = useCallback(
    (opcionLetraOId: string) => {
      const preguntaActual = preguntas[indicePregunta];
      if (!preguntaActual) return;

      setRespuestas((prev) => ({ ...prev, [preguntaActual.id]: opcionLetraOId }));
      guardarRespuestaRef.current(preguntaActual.id, opcionLetraOId, undefined, 1);
    },
    [preguntas, indicePregunta],
  );

  const handleTextResponse = useCallback(
    (valor: string) => {
      const preguntaActual = preguntas[indicePregunta];
      if (!preguntaActual) return;

      setRespuestas((prev) => ({ ...prev, [preguntaActual.id]: valor }));
      guardarRespuestaRef.current(preguntaActual.id, undefined, valor, 1);
    },
    [preguntas, indicePregunta],
  );

  // Manejador específico para Fluidez Verbal (TC-002)
  const handlePalabrasChange = useCallback(
    (nuevasPalabras: string[]) => {
      setPalabrasFV(nuevasPalabras);
      const preguntaId = preguntas[0]?.id || 'FV-01';

      nuevasPalabras.forEach((palabra, idx) => {
        if (palabra.trim() !== '') {
          guardarRespuestaRef.current(preguntaId, undefined, palabra.trim(), idx + 1);
        }
      });
    },
    [preguntas],
  );

  const handleNext = useCallback(() => {
    if (fase === 'instructions') {
      // Avanzar a la fase de práctica antes de iniciar el tiempo oficial (TC-003)
      setFase('practice');
      return;
    }

    if (fase === 'practice') {
      // Iniciar el tiempo oficial tras completar la práctica
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
    if (fase === 'practice') {
      setFase('instructions');
      return;
    }

    if (subprueba?.permitirRetroceso && indicePregunta > 0) {
      setIndicePregunta((i) => i - 1);
    }
  }, [fase, subprueba?.permitirRetroceso, indicePregunta]);

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

  const practiceOptions = [
    { id: 'A', label: 'Opción A (Ejemplo de práctica)' },
    { id: 'B', label: 'Opción B (Ejemplo de práctica)' },
    { id: 'C', label: 'Opción C (Ejemplo de práctica)' },
  ];

  return (
    <TestScreen
      testName={subprueba?.nombre ?? codigo}
      questionNumber={fase === 'active' ? indicePregunta + 1 : 1}
      totalQuestions={fase === 'active' ? totalPreguntas || 1 : 1}
      timeRemainingSeconds={tiempoRestante}
      phase={fase}
      phaseTitle={
        fase === 'instructions'
          ? `Instrucciones: ${subprueba?.nombre ?? codigo}`
          : fase === 'practice'
            ? `Práctica: ${subprueba?.nombre ?? codigo}`
            : fase === 'closed'
              ? 'Subprueba Finalizada'
              : undefined
      }
      phaseDescription={
        fase === 'instructions'
          ? (subprueba?.instrucciones ??
            'Lea atentamente las instrucciones antes de comenzar. Presione "Ir a la práctica" para ver un ejemplo antes de que inicie el tiempo oficial.')
          : fase === 'practice'
            ? 'Esta es una práctica de ejemplo para familiarizarse con el formato. No cuenta para la calificación ni consume tiempo oficial. Cuando esté listo, presione "Comenzar subprueba oficial".'
            : fase === 'closed'
              ? 'El tiempo para esta subprueba ha finalizado o ha completado todas las preguntas. No se admiten más respuestas.'
              : undefined
      }
      prompt={
        fase === 'practice'
          ? esFluidezVerbal
            ? 'Práctica: Escriba una o dos palabras de ejemplo con la letra asignada.'
            : 'Pregunta de práctica: Seleccione una opción para probar la interfaz antes de iniciar el tiempo.'
          : (preguntaActual?.enunciado ?? '')
      }
      options={
        fase === 'practice'
          ? esFluidezVerbal
            ? []
            : practiceOptions
          : (preguntaActual?.opciones.map((o) => ({
              id: o.letra || o.etiqueta || o.id || '',
              label: o.texto || o.etiqueta || '',
            })) ?? [])
      }
      responseType={
        esFluidezVerbal
          ? 'text'
          : preguntaActual?.tipoRespuesta === 'TEXTO' ||
              (preguntaActual && preguntaActual.opciones.length === 0)
            ? 'text'
            : 'selection'
      }
      selectedOption={
        fase === 'practice'
          ? practiceSelection
          : preguntaActual
            ? (respuestas[preguntaActual.id] ?? null)
            : null
      }
      textResponse={
        fase === 'practice'
          ? practiceText
          : preguntaActual
            ? (respuestas[preguntaActual.id] ?? '')
            : ''
      }
      responseArea={
        esFluidezVerbal && fase === 'active' ? (
          <FluidezVerbal letra={letraFV} palabras={palabrasFV} onChange={handlePalabrasChange} />
        ) : undefined
      }
      saveState={saveState}
      primaryActionLabel={
        fase === 'instructions'
          ? 'Ir a la práctica'
          : fase === 'practice'
            ? 'Comenzar subprueba oficial'
            : indicePregunta === totalPreguntas - 1
              ? 'Finalizar'
              : 'Siguiente'
      }
      previousActionLabel="Anterior"
      onPrevious={
        fase === 'practice'
          ? handlePrevious
          : subprueba?.permitirRetroceso && indicePregunta > 0
            ? handlePrevious
            : undefined
      }
      onSelect={fase === 'practice' ? setPracticeSelection : handleSelectOption}
      onTextResponse={fase === 'practice' ? setPracticeText : handleTextResponse}
      onNext={handleNext}
    />
  );
}
