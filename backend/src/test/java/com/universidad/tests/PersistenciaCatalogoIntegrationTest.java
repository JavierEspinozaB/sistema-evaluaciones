
package com.universidad.tests;

import com.universidad.tests.infrastructure.persistence.entity.TestJpaEntity;
import com.universidad.tests.infrastructure.persistence.entity.SubpruebaJpaEntity;
import com.universidad.tests.infrastructure.persistence.repository.TestJpaRepository;
import com.universidad.tests.infrastructure.persistence.repository.SubpruebaJpaRepository;

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

    @Test
    void debeGuardarYConsultarTestConSubpruebas() {

        // 1. Crear un test ficticio con codigo unico
        String codigoTest = "PRUEBA_" + UUID.randomUUID();

        TestJpaEntity test = new TestJpaEntity(
            codigoTest,
            "Test de integracion",
            "Instrumento ficticio para pruebas",
            true
        );

        // 2. Guardar el test en PostgreSQL
        TestJpaEntity testGuardado =
            testRepository.saveAndFlush(test);

        assertThat(testGuardado.getIdTest()).isNotNull();

        // 3. Consultar el test por codigo
        TestJpaEntity testEncontrado =
            testRepository.findByCodigo(codigoTest)
                .orElseThrow();

        assertThat(testEncontrado.getNombre())
            .isEqualTo("Test de integracion");

        // 4. Crear dos subpruebas
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

        // 5. Guardar las subpruebas
        subpruebaRepository.saveAndFlush(subpruebaA);
        subpruebaRepository.saveAndFlush(subpruebaB);

        // 6. Buscar subpruebas por ID del test
        List<SubpruebaJpaEntity> subpruebas =
            subpruebaRepository
                .findByTest_IdTestOrderByCodigoAsc(
                    testGuardado.getIdTest()
                );

        // 7. Verificar los resultados
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
}
