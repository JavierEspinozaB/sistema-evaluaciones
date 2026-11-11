package com.universidad.tests.infrastructure.security;

import com.universidad.tests.domain.exception.AccesoEvaluacionException;
import com.universidad.tests.domain.port.AutorizacionEvaluacionPort;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Sin GEN-06 nunca se interpreta un encabezado de cliente como autorización. */
@Component
@org.springframework.context.annotation.Profile("!seguridad-integrada")
public class AutorizacionEvaluacionDenegadaAdapter implements AutorizacionEvaluacionPort {
    public void exigirAcceso(String evaluacionId) {
        var contexto = RequestContextHolder.getRequestAttributes();
        boolean autenticado =
                contexto instanceof ServletRequestAttributes atributos
                        && atributos.getRequest().getUserPrincipal() != null;
        throw new AccesoEvaluacionException(autenticado);
    }
}
