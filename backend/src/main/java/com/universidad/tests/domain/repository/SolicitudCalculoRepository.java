package com.universidad.tests.domain.repository;

import com.universidad.tests.domain.model.SolicitudCalculoSubprueba;
import java.util.List;
import java.util.Optional;

public interface SolicitudCalculoRepository {
    void registrar(Long idSubprueba);

    List<Long> pendientes(List<String> codigosTest, int limite);

    Optional<SolicitudCalculoSubprueba> bloquearPendiente(Long idSubprueba);

    void completar(Long idSubprueba);
}
