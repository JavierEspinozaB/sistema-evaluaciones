
package com.universidad.tests;

import com.universidad.tests.domain.model.TipoRespuesta;

import com.universidad.tests.infrastructure.persistence.entity.TestJpaEntity;
import com.universidad.tests.infrastructure.persistence.entity.SubpruebaJpaEntity;
import com.universidad.tests.infrastructure.persistence.entity.VersionFormularioJpaEntity;
import com.universidad.tests.infrastructure.persistence.entity.ConfiguracionSubpruebaJpaEntity;
import com.universidad.tests.infrastructure.persistence.entity.PreguntaJpaEntity;
import com.universidad.tests.infrastructure.persistence.entity.OpcionRespuestaJpaEntity;

import com.universidad.tests.infrastructure.persistence.repository.TestJpaRepository;
import com.universidad.tests.infrastructure.persistence.repository.SubpruebaJpaRepository;
import com.universidad.tests.infrastructure.persistence.repository.VersionFormularioJpaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PersistenciaCatalogoIntegrationTest {

    @Autowired
    private TestJpaRepository testRepository;

    @Autowired
    private SubpruebaJpaRepository subpruebaRepository;

    @Autowired
    private VersionFormularioJpaRepository versionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // =====================================================
    // PRUEBA 1: Persistencia de tests y subpruebas
    // =====================================================

    @Test
    void debeGuardarYConsultarTestConSubpruebas() {

        String codigoTest = "PRUEBA_" + UUID.randomUUID();

        TestJpaEntity test = new TestJpaEntity(
            codigoTest,
            "Test de integracion",
            "Instrumento ficticio para pruebas",
            true
        );

        TestJpaEntity testGuardado =
            testRepository.saveAndFlush(test);

        assertThat(testGuardado.getIdTest()).isNotNull();

        TestJpaEntity testEncontrado =
            testRepository.findByCodigo(codigoTest)
                .orElseThrow();

        assertThat(testEncontrado.getNombre())
            .isEqualTo("Test de integracion");

        SubpruebaJpaEntity subpruebaA =
            new SubpruebaJpaEntity(
                testGuardado,
                "SUB_A",
                "Subprueba A",
                "Primera subprueba ficticia"
            );

        SubpruebaJpaEntity subpruebaB =
            new SubpruebaJpaEntity(
                testGuardado,
                "SUB_B",
                "Subprueba B",
                "Segunda subprueba ficticia"
            );

        subpruebaRepository.saveAndFlush(subpruebaA);
        subpruebaRepository.saveAndFlush(subpruebaB);

        List<SubpruebaJpaEntity> subpruebas =
            subpruebaRepository
                .findByTest_IdTestOrderByCodigoAsc(
                    testGuardado.getIdTest()
                );

        assertThat(subpruebas).hasSize(2);

        assertThat(subpruebas)
            .extracting(SubpruebaJpaEntity::getCodigo)
            .containsExactly("SUB_A", "SUB_B");

        assertThat(
            subpruebaRepository
                .existsByTest_IdTestAndCodigo(
                    testGuardado.getIdTest(),
                    "SUB_A"
                )
        ).isTrue();
    }

    // =====================================================
    // PRUEBA 2: Dos versiones del mismo test
    // =====================================================

    @Test
    void debeGuardarDosVersionesDelMismoTest() {

        String codigoTest = "VERSIONES_" + UUID.randomUUID();

        TestJpaEntity test = testRepository.saveAndFlush(
            new TestJpaEntity(
                codigoTest,
                "Test de versionado",
                "Instrumento ficticio para pruebas",
                true
            )
        );

        VersionFormularioJpaEntity version1 =
            versionRepository.saveAndFlush(
                new VersionFormularioJpaEntity(
                    test,
                    "v1",
                    "Primera version",
                    "FUENTE-001"
                )
            );

        VersionFormularioJpaEntity version2 =
            versionRepository.saveAndFlush(
                new VersionFormularioJpaEntity(
                    test,
                    "v2",
                    "Segunda version",
                    "FUENTE-002"
                )
            );

        List<VersionFormularioJpaEntity> versiones =
            versionRepository
                .findByTest_IdTestOrderByCodigoVersionAsc(
                    test.getIdTest()
                );

        assertThat(versiones).hasSize(2);

        assertThat(version1.getIdVersionFormulario())
            .isNotEqualTo(version2.getIdVersionFormulario());

        assertThat(version1.getIdTest())
            .isEqualTo(test.getIdTest());

        assertThat(version2.getIdTest())
            .isEqualTo(test.getIdTest());

        assertThat(versiones)
            .extracting(VersionFormularioJpaEntity::getCodigoVersion)
            .containsExactly("v1", "v2");

        assertThat(version1.getFuenteRef())
            .isEqualTo("FUENTE-001");

        assertThat(version2.getFuenteRef())
            .isEqualTo("FUENTE-002");
    }

    // =====================================================
    // PRUEBA 3: Independencia del contenido entre versiones
    // =====================================================

    @Test
    void debeMantenerContenidoIndependienteEntreVersiones() {

        // 1. Crear un test

        TestJpaEntity test = testRepository.saveAndFlush(
            new TestJpaEntity(
                "CONTENIDO_" + UUID.randomUUID(),
                "Test de contenido versionado",
                "Prueba de integridad entre versiones",
                true
            )
        );

        // 2. Crear una subprueba

        SubpruebaJpaEntity subprueba =
            subpruebaRepository.saveAndFlush(
                new SubpruebaJpaEntity(
                    test,
                    "SUB_CONTENIDO",
                    "Subprueba de contenido",
                    "Subprueba utilizada para verificar versiones"
                )
            );

        // 3. Crear dos versiones del mismo test

        VersionFormularioJpaEntity version1 =
            versionRepository.saveAndFlush(
                new VersionFormularioJpaEntity(
                    test,
                    "v1",
                    "Version original",
                    "FUENTE-001"
                )
            );

        VersionFormularioJpaEntity version2 =
            versionRepository.saveAndFlush(
                new VersionFormularioJpaEntity(
                    test,
                    "v2",
                    "Version actualizada",
                    "FUENTE-002"
                )
            );

        // 4. Crear configuraciones independientes

        ConfiguracionSubpruebaJpaEntity config1 =
            new ConfiguracionSubpruebaJpaEntity(
                version1,
                subprueba,
                1,
                null,
                TipoRespuesta.SELECCION,
                "Instrucciones de la version 1",
                null,
                null
            );

        ConfiguracionSubpruebaJpaEntity config2 =
            new ConfiguracionSubpruebaJpaEntity(
                version2,
                subprueba,
                1,
                null,
                TipoRespuesta.SELECCION,
                "Instrucciones de la version 2",
                null,
                null
            );

        entityManager.persist(config1);
        entityManager.persist(config2);
        entityManager.flush();

        // 5. Crear una pregunta en cada version

        PreguntaJpaEntity pregunta1 =
            new PreguntaJpaEntity(
                config1,
                "1",
                1,
                "Pregunta original de la version 1",
                null
            );

        PreguntaJpaEntity pregunta2 =
            new PreguntaJpaEntity(
                config2,
                "1",
                1,
                "Pregunta actualizada de la version 2",
                null
            );

        entityManager.persist(pregunta1);
        entityManager.persist(pregunta2);
        entityManager.flush();

        // 6. Crear una opcion para cada pregunta

        OpcionRespuestaJpaEntity opcion1 =
            new OpcionRespuestaJpaEntity(
                pregunta1,
                "A",
                1,
                "Respuesta de la version 1",
                null
            );

        OpcionRespuestaJpaEntity opcion2 =
            new OpcionRespuestaJpaEntity(
                pregunta2,
                "A",
                1,
                "Respuesta de la version 2",
                null
            );

        entityManager.persist(opcion1);
        entityManager.persist(opcion2);
        entityManager.flush();

        // 7. Guardar los IDs para realizar consultas

        Long idVersion1 =
            version1.getIdVersionFormulario();

        Long idVersion2 =
            version2.getIdVersionFormulario();

        Long idConfig1 =
            config1.getIdConfiguracionSubprueba();

        Long idConfig2 =
            config2.getIdConfiguracionSubprueba();

        // Limpiar el contexto de persistencia

        entityManager.clear();

        // 8. Consultar preguntas de la version 1

        List<PreguntaJpaEntity> preguntasV1 =
            entityManager.createQuery(
                    "SELECT p FROM PreguntaJpaEntity p " +
                        "WHERE p.configuracionSubprueba.versionFormulario." +
                        "idVersionFormulario = :idVersion",
                    PreguntaJpaEntity.class
                )
                .setParameter("idVersion", idVersion1)
                .getResultList();

        // 9. Consultar preguntas de la version 2

        List<PreguntaJpaEntity> preguntasV2 =
            entityManager.createQuery(
                    "SELECT p FROM PreguntaJpaEntity p " +
                        "WHERE p.configuracionSubprueba.versionFormulario." +
                        "idVersionFormulario = :idVersion",
                    PreguntaJpaEntity.class
                )
                .setParameter("idVersion", idVersion2)
                .getResultList();

        // 10. Verificar independencia de configuraciones

        assertThat(idConfig1).isNotEqualTo(idConfig2);

        assertThat(
            entityManager.find(
                ConfiguracionSubpruebaJpaEntity.class,
                idConfig1
            ).getInstruccionesRef()
        ).isEqualTo("Instrucciones de la version 1");

        assertThat(
            entityManager.find(
                ConfiguracionSubpruebaJpaEntity.class,
                idConfig2
            ).getInstruccionesRef()
        ).isEqualTo("Instrucciones de la version 2");

        // 11. Verificar independencia de preguntas

        assertThat(preguntasV1).hasSize(1);
        assertThat(preguntasV2).hasSize(1);

        assertThat(preguntasV1.get(0).getEnunciado())
            .isEqualTo("Pregunta original de la version 1");

        assertThat(preguntasV2.get(0).getEnunciado())
            .isEqualTo("Pregunta actualizada de la version 2");

        assertThat(preguntasV1.get(0).getIdPregunta())
            .isNotEqualTo(preguntasV2.get(0).getIdPregunta());

        assertThat(preguntasV1.get(0).getNumeroVisible())
            .isEqualTo("1");

        assertThat(preguntasV2.get(0).getNumeroVisible())
            .isEqualTo("1");

        // 12. Obtener los IDs de las preguntas

        Long idPregunta1 =
            preguntasV1.get(0).getIdPregunta();

        Long idPregunta2 =
            preguntasV2.get(0).getIdPregunta();

        // 13. Consultar opciones de la version 1

        List<OpcionRespuestaJpaEntity> opcionesV1 =
            entityManager.createQuery(
                    "SELECT o FROM OpcionRespuestaJpaEntity o " +
                        "WHERE o.pregunta.idPregunta = :idPregunta",
                    OpcionRespuestaJpaEntity.class
                )
                .setParameter("idPregunta", idPregunta1)
                .getResultList();

        // 14. Consultar opciones de la version 2

        List<OpcionRespuestaJpaEntity> opcionesV2 =
            entityManager.createQuery(
                    "SELECT o FROM OpcionRespuestaJpaEntity o " +
                        "WHERE o.pregunta.idPregunta = :idPregunta",
                    OpcionRespuestaJpaEntity.class
                )
                .setParameter("idPregunta", idPregunta2)
                .getResultList();

        // 15. Verificar independencia de opciones

        assertThat(opcionesV1).hasSize(1);
        assertThat(opcionesV2).hasSize(1);

        assertThat(opcionesV1.get(0).getTexto())
            .isEqualTo("Respuesta de la version 1");

        assertThat(opcionesV2.get(0).getTexto())
            .isEqualTo("Respuesta de la version 2");

        assertThat(opcionesV1.get(0).getIdOpcion())
            .isNotEqualTo(opcionesV2.get(0).getIdOpcion());
    }
}
