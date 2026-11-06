package com.universidad.tests.domain.model;

import com.universidad.tests.domain.exception.RecursoNoEncontradoException;
import com.universidad.tests.domain.exception.SubpruebaFueraDeOrdenException;
import java.util.Collections;
import java.util.List;

public class Evaluacion {
    private final String id;
    private final String sujetoId;
    private String estado;
    private final List<EvaluacionSubprueba> subpruebas;

    public Evaluacion(
            String id, String sujetoId, String estado, List<EvaluacionSubprueba> subpruebas) {
        this.id = id;
        this.sujetoId = sujetoId;
        this.estado = estado;
        this.subpruebas = subpruebas == null ? Collections.emptyList() : List.copyOf(subpruebas);
    }

    public String getId() {
        return id;
    }

    public String getSujetoId() {
        return sujetoId;
    }

    public String getEstado() {
        return estado;
    }

    public List<EvaluacionSubprueba> getSubpruebas() {
        return subpruebas;
    }

    public EvaluacionSubprueba buscarSubprueba(String codigo) {
        return subpruebas.stream()
                .filter(s -> s.getCodigo().equalsIgnoreCase(codigo))
                .findFirst()
                .orElseThrow(
                        () ->
                                new RecursoNoEncontradoException(
                                        "SUBPRUEBA_NO_ENCONTRADA",
                                        "Subprueba no encontrada: " + codigo));
    }

    public void validarOrdenSubprueba(String codigo) {
        EvaluacionSubprueba objetivo = buscarSubprueba(codigo);
        for (EvaluacionSubprueba sub : subpruebas) {
            if (sub.getOrden() < objetivo.getOrden()
                    && sub.getEstado() != EstadoSubprueba.CERRADA) {
                throw new SubpruebaFueraDeOrdenException(
                        "SUBPRUEBA_FUERA_DE_ORDEN",
                        "No se permite iniciar la subprueba "
                                + codigo
                                + " porque la subprueba anterior "
                                + sub.getCodigo()
                                + " aún no ha finalizado.");
            }
        }
    }
}
