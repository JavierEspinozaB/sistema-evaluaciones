package com.universidad.tests.infrastructure.persistence.adapter;

import com.universidad.tests.domain.model.SolicitudCalculoSubprueba;
import com.universidad.tests.domain.repository.SolicitudCalculoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SolicitudCalculoAdapter implements SolicitudCalculoRepository {
    private final JdbcTemplate jdbc;

    public SolicitudCalculoAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void registrar(Long idSubprueba) {
        jdbc.update(
                "INSERT INTO solicitud_calculo_subprueba(id_subprueba,evaluacion_id,codigo_test,codigo_subprueba,fecha_cierre) SELECT s.id,s.evaluacion_id,t.codigo,s.codigo,s.fecha_cierre FROM evaluacion_subprueba s JOIN evaluacion e ON e.id=s.evaluacion_id JOIN test t ON t.id_test::text=e.test_id WHERE s.id=? AND s.estado='CERRADA' ON CONFLICT(id_subprueba) DO NOTHING",
                idSubprueba);
    }

    public List<Long> pendientes(List<String> codigosTest, int limite) {
        if (codigosTest.isEmpty()) return List.of();
        var parametros = new java.util.ArrayList<Object>();
        codigosTest.forEach(codigo -> parametros.add(codigo.toUpperCase(java.util.Locale.ROOT)));
        parametros.add(limite);
        String marcadores =
                String.join(",", java.util.Collections.nCopies(codigosTest.size(), "?"));
        return jdbc.queryForList(
                "SELECT id_subprueba FROM solicitud_calculo_subprueba WHERE procesada_en IS NULL AND upper(codigo_test) IN ("
                        + marcadores
                        + ") ORDER BY fecha_cierre,id_subprueba LIMIT ?",
                Long.class,
                parametros.toArray());
    }

    public Optional<SolicitudCalculoSubprueba> bloquearPendiente(Long idSubprueba) {
        return jdbc
                .query(
                        "SELECT * FROM solicitud_calculo_subprueba WHERE id_subprueba=? AND procesada_en IS NULL FOR UPDATE SKIP LOCKED",
                        (rs, n) ->
                                new SolicitudCalculoSubprueba(
                                        rs.getLong("id_subprueba"),
                                        rs.getString("evaluacion_id"),
                                        rs.getString("codigo_test"),
                                        rs.getString("codigo_subprueba"),
                                        rs.getTimestamp("fecha_cierre").toInstant()),
                        idSubprueba)
                .stream()
                .findFirst();
    }

    public void completar(Long idSubprueba) {
        jdbc.update(
                "UPDATE solicitud_calculo_subprueba SET procesada_en=CURRENT_TIMESTAMP WHERE id_subprueba=? AND procesada_en IS NULL",
                idSubprueba);
    }
}
