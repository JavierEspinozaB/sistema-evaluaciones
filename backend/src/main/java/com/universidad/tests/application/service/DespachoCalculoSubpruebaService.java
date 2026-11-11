package com.universidad.tests.application.service;

import com.universidad.tests.domain.port.EvaluadorSubpruebaPort;
import com.universidad.tests.domain.repository.SolicitudCalculoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DespachoCalculoSubpruebaService {
    private final SolicitudCalculoRepository repository;
    private final List<EvaluadorSubpruebaPort> evaluadores;

    public DespachoCalculoSubpruebaService(
            SolicitudCalculoRepository repository, List<EvaluadorSubpruebaPort> evaluadores) {
        this.repository = repository;
        this.evaluadores = List.copyOf(evaluadores);
    }

    public List<Long> pendientes(int limite) {
        return repository.pendientes(
                evaluadores.stream().map(EvaluadorSubpruebaPort::codigoTest).distinct().toList(),
                limite);
    }

    @Transactional
    public void procesar(Long idSubprueba) {
        var solicitud = repository.bloquearPendiente(idSubprueba);
        if (solicitud.isEmpty()) return;
        var estrategias =
                evaluadores.stream()
                        .filter(e -> e.codigoTest().equalsIgnoreCase(solicitud.get().codigoTest()))
                        .toList();
        if (estrategias.isEmpty())
            return; // La petición durable espera a que se integre el evaluador del test.
        if (estrategias.size() != 1)
            throw new IllegalStateException("Hay más de un evaluador para el mismo test.");
        estrategias.get(0).calcular(solicitud.get());
        repository.completar(idSubprueba);
    }
}
