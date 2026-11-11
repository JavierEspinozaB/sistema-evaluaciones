package com.universidad.tests.infrastructure.persistence.adapter;

import com.universidad.tests.domain.model.Opcion;
import com.universidad.tests.domain.model.PreguntaAplicacion;
import com.universidad.tests.domain.model.TipoRespuestaAplicacion;
import com.universidad.tests.domain.repository.PreguntaRepositoryAplicacion;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PreguntaRepositoryAdapter implements PreguntaRepositoryAplicacion {
    private final JdbcTemplate jdbc;

    public PreguntaRepositoryAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PreguntaAplicacion> listarPorConfiguracion(Long configuracionId) {
        return cargar(configuracionId, null);
    }

    public Optional<PreguntaAplicacion> buscarPorIdYConfiguracion(String id, Long configuracionId) {
        try {
            return cargar(configuracionId, Long.valueOf(id)).stream().findFirst();
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private List<PreguntaAplicacion> cargar(Long configuracionId, Long preguntaId) {
        String sql =
                "SELECT p.*, s.codigo, c.tipo_respuesta FROM pregunta p JOIN configuracion_subprueba c ON c.id_configuracion_subprueba=p.id_configuracion_subprueba JOIN subprueba s ON s.id_subprueba=c.id_subprueba WHERE c.id_configuracion_subprueba=?";
        Object[] args =
                preguntaId == null
                        ? new Object[] {configuracionId}
                        : new Object[] {configuracionId, preguntaId};
        if (preguntaId != null) sql += " AND p.id_pregunta=?";
        return jdbc.query(
                sql + " ORDER BY p.orden_presentacion",
                (rs, n) -> {
                    String id = rs.getString("id_pregunta");
                    List<Opcion> opciones =
                            jdbc.query(
                                    "SELECT id_opcion,codigo,texto,orden_presentacion,recurso_ref FROM opcion_respuesta WHERE id_pregunta=? ORDER BY orden_presentacion",
                                    (o, i) ->
                                            new Opcion(
                                                    o.getString(1),
                                                    o.getString(2),
                                                    o.getString(3),
                                                    o.getInt(4),
                                                    o.getString(5)),
                                    Long.valueOf(id));
                    return new PreguntaAplicacion(
                            id,
                            rs.getString("codigo"),
                            rs.getInt("orden_presentacion"),
                            rs.getString("enunciado"),
                            rs.getString("recurso_ref"),
                            "SELECCION".equals(rs.getString("tipo_respuesta"))
                                    ? TipoRespuestaAplicacion.SELECCION
                                    : TipoRespuestaAplicacion.TEXTO,
                            rs.getInt("orden_presentacion"),
                            opciones);
                },
                args);
    }
}
