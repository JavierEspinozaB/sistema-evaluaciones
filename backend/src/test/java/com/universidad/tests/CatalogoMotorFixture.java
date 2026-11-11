package com.universidad.tests;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/** Solo datos sintéticos; se construyen en borrador y se publican por el ciclo de GEN-07. */
record CatalogoMotorFixture(
        long testId,
        long versionId,
        long fv,
        long voc1,
        long voc2,
        long texto,
        long pregunta1,
        long preguntaAjena,
        long preguntaOtraVersion,
        String codigoTest) {
    static CatalogoMotorFixture crear(JdbcTemplate jdbc) {
        String codigo = "SINTETICO_" + UUID.randomUUID();
        long test =
                insertar(
                        jdbc,
                        "INSERT INTO test(codigo,nombre) VALUES (?,'Test sintético') RETURNING id_test",
                        codigo);
        long v = version(jdbc, test, "v1");
        long otra = version(jdbc, test, "v2");
        long sf = sub(jdbc, test, "FV"),
                s1 = sub(jdbc, test, "VOC1"),
                s2 = sub(jdbc, test, "VOC2"),
                st = sub(jdbc, test, "TEXTO");
        long fv = config(jdbc, v, sf, 1, "TEXTO_LIBRE", 75);
        long c1 = config(jdbc, v, s1, 2, "SELECCION", null);
        long c2 = config(jdbc, v, s2, 3, "SELECCION", null);
        long ct = config(jdbc, v, st, 4, "TEXTO_LIBRE", 1);
        long p1 = pregunta(jdbc, c1, "Pregunta sintética de la edición aplicada");
        long p2 = pregunta(jdbc, c2, "Pregunta de otra subprueba");
        pregunta(jdbc, ct, "Texto sintético");
        long co = config(jdbc, otra, s1, 1, "SELECCION", null);
        long po = pregunta(jdbc, co, "Pregunta de otra edición");
        publicar(jdbc, v);
        publicar(jdbc, otra);
        return new CatalogoMotorFixture(test, v, fv, c1, c2, ct, p1, p2, po, codigo);
    }

    private static long insertar(JdbcTemplate j, String sql, Object... args) {
        return j.queryForObject(sql, Long.class, args);
    }

    private static long version(JdbcTemplate j, long test, String codigo) {
        return insertar(
                j,
                "INSERT INTO version_formulario(id_test,codigo_version,fuente_ref) VALUES (?,?, 'fixture-sintetica') RETURNING id_version_formulario",
                test,
                codigo);
    }

    private static long sub(JdbcTemplate j, long test, String codigo) {
        return insertar(
                j,
                "INSERT INTO subprueba(id_test,codigo,nombre) VALUES (?,?,?) RETURNING id_subprueba",
                test,
                codigo,
                codigo);
    }

    private static long config(
            JdbcTemplate j, long v, long sub, int orden, String tipo, Integer max) {
        return insertar(
                j,
                "INSERT INTO configuracion_subprueba(id_version_formulario,id_subprueba,orden_aplicacion,tiempo_limite_segundos,tipo_respuesta,instrucciones_ref,maximo_entradas) VALUES (?,?,?,300,?,'Instrucción sintética con la letra M',?) RETURNING id_configuracion_subprueba",
                v,
                sub,
                orden,
                tipo,
                max);
    }

    private static long pregunta(JdbcTemplate j, long c, String texto) {
        long p =
                insertar(
                        j,
                        "INSERT INTO pregunta(id_configuracion_subprueba,numero_visible,orden_presentacion,enunciado,recurso_ref) VALUES (?, '1',1,?,'/ficticio/pregunta.svg') RETURNING id_pregunta",
                        c,
                        texto);
        if (j.queryForObject(
                        "SELECT tipo_respuesta FROM configuracion_subprueba WHERE id_configuracion_subprueba=?",
                        String.class,
                        c)
                .equals("SELECCION")) {
            j.update(
                    "INSERT INTO opcion_respuesta(id_pregunta,codigo,orden_presentacion,texto,recurso_ref) VALUES (?,'A',1,'Alternativa sintética A','/ficticio/opcion.svg'),(?,'B',2,'Alternativa sintética B',null)",
                    p,
                    p);
        }
        return p;
    }

    private static void publicar(JdbcTemplate j, long version) {
        j.update(
                "UPDATE version_formulario SET estado='PUBLICADA',publicada_en=now(),evidencia_validacion='Fixture sintética de pruebas' WHERE id_version_formulario=?",
                version);
    }

    void evaluacion(JdbcTemplate jdbc, String id, String codigo, long config) {
        jdbc.update(
                "INSERT INTO evaluacion(id,sujeto_id,estado,creado_en,actualizado_en,test_id,periodo_id,version_formulario_id,fecha_inicio) VALUES (?,'FICTICIO','EN_CURSO',now(),now(),?,'1',?,now())",
                id,
                Long.toString(testId),
                Long.toString(versionId));
        agregarSubprueba(jdbc, id, codigo, config);
    }

    void agregarSubprueba(JdbcTemplate jdbc, String id, String codigo, long config) {
        jdbc.update(
                "INSERT INTO evaluacion_subprueba(evaluacion_id,configuracion_id,codigo,nombre,orden,duracion_segundos,estado,instrucciones,permitir_retroceso,letra_inicial) VALUES (?,?,?, 'Snapshot sintético',1,999,'PENDIENTE','Snapshot no autoritativo',true,?)",
                id,
                config,
                codigo,
                codigo.equals("FV") ? "P" : null);
    }
}
