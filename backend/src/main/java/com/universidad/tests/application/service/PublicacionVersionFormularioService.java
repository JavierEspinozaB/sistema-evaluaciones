
package com.universidad.tests.application.service;

import com.universidad.tests.domain.model.ConfiguracionSubprueba;
import com.universidad.tests.domain.model.EstadoVersionFormulario;
import com.universidad.tests.domain.model.Pregunta;
import com.universidad.tests.domain.model.Subprueba;
import com.universidad.tests.domain.model.TipoRespuesta;
import com.universidad.tests.domain.model.VersionFormulario;

import com.universidad.tests.domain.repository.ConfiguracionSubpruebaRepository;
import com.universidad.tests.domain.repository.OpcionRespuestaRepository;
import com.universidad.tests.domain.repository.PreguntaRepository;
import com.universidad.tests.domain.repository.SubpruebaRepository;
import com.universidad.tests.domain.repository.VersionFormularioRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicacionVersionFormularioService {

    private final VersionFormularioRepository versionRepository;
    private final ConfiguracionSubpruebaRepository configuracionRepository;
    private final SubpruebaRepository subpruebaRepository;
    private final PreguntaRepository preguntaRepository;
    private final OpcionRespuestaRepository opcionRepository;

    public PublicacionVersionFormularioService(
        VersionFormularioRepository versionRepository,
        ConfiguracionSubpruebaRepository configuracionRepository,
        SubpruebaRepository subpruebaRepository,
        PreguntaRepository preguntaRepository,
        OpcionRespuestaRepository opcionRepository) {

        this.versionRepository = versionRepository;
        this.configuracionRepository = configuracionRepository;
        this.subpruebaRepository = subpruebaRepository;
        this.preguntaRepository = preguntaRepository;
        this.opcionRepository = opcionRepository;
    }

    @Transactional
    public VersionFormulario publicar(
        Long idVersionFormulario,
        String evidenciaValidacion) {

        VersionFormulario version = buscarVersion(idVersionFormulario);

        // Solo se pueden publicar borradores.
        if (version.getEstado() != EstadoVersionFormulario.BORRADOR) {
            throw new IllegalStateException(
                "La version debe estar en BORRADOR");
        }

        if (evidenciaValidacion == null
            || evidenciaValidacion.isBlank()) {
            throw new IllegalArgumentException(
                "La evidencia de validacion es obligatoria");
        }

        // Verificar que el formulario tenga contenido valido.
        validarContenido(version);

        // Cambiar el estado del modelo de dominio.
        version.publicar(evidenciaValidacion);

        // Persistir la publicacion.
        return versionRepository.guardar(version);
    }

    @Transactional
    public VersionFormulario retirar(Long idVersionFormulario) {

        VersionFormulario version = buscarVersion(idVersionFormulario);

        if (version.getEstado() != EstadoVersionFormulario.PUBLICADA) {
            throw new IllegalStateException(
                "Solo se puede retirar una version PUBLICADA");
        }

        version.retirar();

        return versionRepository.guardar(version);
    }

    private void validarContenido(VersionFormulario version) {

        Long idVersion = version.getIdVersionFormulario();

        List<ConfiguracionSubprueba> configuraciones =
            configuracionRepository.listarPorVersion(idVersion);

        // Una version no puede publicarse sin configuraciones.
        if (configuraciones.isEmpty()) {
            throw new IllegalStateException(
                "No se puede publicar una version sin subpruebas");
        }

        for (ConfiguracionSubprueba configuracion : configuraciones) {

            // Confirmar que la subprueba pertenece al mismo test.
            Subprueba subprueba = subpruebaRepository
                .buscarPorId(configuracion.getIdSubprueba())
                .orElseThrow(() -> new NoSuchElementException(
                    "No existe una subprueba de la configuracion"));

            if (!Objects.equals(
                subprueba.getIdTest(),
                version.getIdTest())) {

                throw new IllegalStateException(
                    "Una subprueba pertenece a otro test");
            }

            // Recuperar preguntas de la configuracion.
            List<Pregunta> preguntas =
                preguntaRepository.listarPorConfiguracion(
                    configuracion.getIdConfiguracionSubprueba()
                );

            if (preguntas.isEmpty()) {
                throw new IllegalStateException(
                    "La subprueba " + subprueba.getCodigo()
                        + " no contiene preguntas");
            }

            // Las preguntas de seleccion requieren alternativas.
            if (configuracion.getTipoRespuesta()
                == TipoRespuesta.SELECCION) {

                for (Pregunta pregunta : preguntas) {

                    if (opcionRepository
                        .listarPorPregunta(pregunta.getIdPregunta())
                        .isEmpty()) {

                        throw new IllegalStateException(
                            "La pregunta "
                                + pregunta.getNumeroVisible()
                                + " no contiene opciones");
                    }
                }
            }
        }
    }

    private VersionFormulario buscarVersion(Long idVersionFormulario) {

        if (idVersionFormulario == null
            || idVersionFormulario <= 0) {
            throw new IllegalArgumentException(
                "El ID de version debe ser positivo");
        }

        return versionRepository.buscarPorId(idVersionFormulario)
            .orElseThrow(() -> new NoSuchElementException(
                "No existe la version con ID: "
                    + idVersionFormulario));
    }
}
