package com.universidad.tests.domain.repository;

import com.universidad.tests.domain.model.PreguntaAplicacion;
import java.util.List;
import java.util.Optional;

/** Proyección de lectura del catálogo GEN-07 para una configuración de la edición aplicada. */
public interface PreguntaRepositoryAplicacion {
    List<PreguntaAplicacion> listarPorConfiguracion(Long configuracionId);

    Optional<PreguntaAplicacion> buscarPorIdYConfiguracion(String id, Long configuracionId);
}
