package com.universidad.tests.config;

import com.universidad.tests.application.service.DespachoCalculoSubpruebaService;
import com.universidad.tests.application.service.MotorSubpruebasService;
import com.universidad.tests.domain.repository.EvaluacionRepository;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
        name = "motor.temporizador.habilitado",
        havingValue = "true",
        matchIfMissing = true)
public class TemporizadorSubpruebasConfig {
    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(TemporizadorSubpruebasConfig.class);
    private final EvaluacionRepository evaluaciones;
    private final MotorSubpruebasService motor;
    private final DespachoCalculoSubpruebaService despacho;
    private final Clock clock;

    public TemporizadorSubpruebasConfig(
            EvaluacionRepository evaluaciones,
            MotorSubpruebasService motor,
            DespachoCalculoSubpruebaService despacho,
            Clock clock) {
        this.evaluaciones = evaluaciones;
        this.motor = motor;
        this.despacho = despacho;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${motor.temporizador.intervalo-ms:1000}")
    public void cerrarVencidas() {
        for (String id : evaluaciones.listarConVencidas(clock.instant(), 100)) {
            try {
                motor.procesarVencimiento(id);
            } catch (RuntimeException ex) {
                LOG.error("No se pudo procesar el vencimiento de una evaluación", ex);
            }
        }
    }

    @Scheduled(fixedDelayString = "${motor.calculo.intervalo-ms:1000}")
    public void despacharCalculos() {
        for (Long id : despacho.pendientes(100)) {
            try {
                despacho.procesar(id);
            } catch (RuntimeException ex) {
                LOG.error("Solicitud de cálculo pendiente; se reintentará", ex);
            }
        }
    }
}
