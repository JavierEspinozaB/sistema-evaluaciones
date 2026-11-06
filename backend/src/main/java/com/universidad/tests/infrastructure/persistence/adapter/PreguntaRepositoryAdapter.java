package com.universidad.tests.infrastructure.persistence.adapter;

import com.universidad.tests.domain.model.Opcion;
import com.universidad.tests.domain.model.Pregunta;
import com.universidad.tests.domain.model.TipoRespuesta;
import com.universidad.tests.domain.repository.PreguntaRepository;
import com.universidad.tests.infrastructure.persistence.entity.OpcionPreguntaEntity;
import com.universidad.tests.infrastructure.persistence.entity.PreguntaCatalogoEntity;
import com.universidad.tests.infrastructure.persistence.repository.SpringDataPreguntaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PreguntaRepositoryAdapter implements PreguntaRepository {

    private final SpringDataPreguntaRepository preguntaRepo;

    public PreguntaRepositoryAdapter(SpringDataPreguntaRepository preguntaRepo) {
        this.preguntaRepo = preguntaRepo;
    }

    @Override
    public List<Pregunta> listarPorSubprueba(String subpruebaCodigo) {
        return preguntaRepo.findBySubpruebaCodigoIgnoreCaseOrderByOrdenAsc(subpruebaCodigo).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Pregunta> buscarPorId(String id) {
        return preguntaRepo.findById(id).map(this::toDomain);
    }

    private Pregunta toDomain(PreguntaCatalogoEntity entity) {
        List<Opcion> opciones = entity.getOpciones().stream().map(this::toDomainOpcion).toList();

        return new Pregunta(
                entity.getId(),
                entity.getSubpruebaCodigo(),
                entity.getNumero(),
                entity.getEnunciado(),
                entity.getRecursoMultimedia(),
                TipoRespuesta.valueOf(entity.getTipoRespuesta()),
                entity.getClaveCorrecta(),
                entity.getOrden(),
                opciones);
    }

    private Opcion toDomainOpcion(OpcionPreguntaEntity entity) {
        return new Opcion(
                entity.getId(), entity.getEtiqueta(), entity.getTexto(), entity.getOrden());
    }
}
