package com.universidad.tests.domain.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

// Permisos concedidos a cada rol activo (Documento general §2.3: lo no concedido se deniega).
// PENDIENTE GEN-06-BE: la documentacion aprobada aun no publica la matriz rol -> permisos;
// mientras tanto ningun rol recibe permisos y toda operacion protegida responde SIN_PERMISO.
// No se completa con valores supuestos.
public final class MatrizPermisos {

    private final Map<Rol, Set<String>> permisosPorRol;

    public MatrizPermisos(Map<Rol, Set<String>> permisosPorRol) {
        Map<Rol, Set<String>> copia = new EnumMap<>(Rol.class);
        if (permisosPorRol != null) {
            permisosPorRol.forEach((rol, permisos) -> copia.put(rol, Set.copyOf(permisos)));
        }
        this.permisosPorRol = Map.copyOf(copia);
    }

    public static MatrizPermisos aprobada() {
        return new MatrizPermisos(Map.of());
    }

    public Set<String> permisosDe(Rol rol) {
        return permisosPorRol.getOrDefault(rol, Set.of());
    }
}
