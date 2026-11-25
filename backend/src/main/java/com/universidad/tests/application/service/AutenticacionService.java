package com.universidad.tests.application.service;

import com.universidad.tests.domain.exception.CredencialesInvalidasException;
import com.universidad.tests.domain.exception.NoAutenticadoException;
import com.universidad.tests.domain.exception.RolActivoNoDeterminadoException;
import com.universidad.tests.domain.model.Cuenta;
import com.universidad.tests.domain.model.MatrizPermisos;
import com.universidad.tests.domain.model.Rol;
import com.universidad.tests.domain.model.Sesion;
import com.universidad.tests.domain.port.HashContrasenasPort;
import com.universidad.tests.domain.port.TokensSesionPort;
import com.universidad.tests.domain.repository.CuentaRepository;
import com.universidad.tests.domain.repository.SesionRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// GEN-06-BE: abre, valida y cierra sesiones (E14, E15 y Bearer de las demas llamadas).
// Los endpoints E13-E17 los expone GEN-02-BE usando este servicio.
@Service
public class AutenticacionService {

    private final CuentaRepository cuentaRepository;
    private final SesionRepository sesionRepository;
    private final HashContrasenasPort hashContrasenas;
    private final TokensSesionPort tokens;
    private final MatrizPermisos matrizPermisos;

    // Resumen de una contrasena aleatoria: se verifica contra el cuando el correo no existe
    // para que la respuesta tarde lo mismo y no revele si la cuenta existe
    private final String resumenSinCuenta;

    @Autowired
    public AutenticacionService(
            CuentaRepository cuentaRepository,
            SesionRepository sesionRepository,
            HashContrasenasPort hashContrasenas,
            TokensSesionPort tokens) {

        this(
                cuentaRepository,
                sesionRepository,
                hashContrasenas,
                tokens,
                MatrizPermisos.aprobada());
    }

    AutenticacionService(
            CuentaRepository cuentaRepository,
            SesionRepository sesionRepository,
            HashContrasenasPort hashContrasenas,
            TokensSesionPort tokens,
            MatrizPermisos matrizPermisos) {

        this.cuentaRepository = cuentaRepository;
        this.sesionRepository = sesionRepository;
        this.hashContrasenas = hashContrasenas;
        this.tokens = tokens;
        this.matrizPermisos = matrizPermisos;
        this.resumenSinCuenta = hashContrasenas.crear(tokens.generar().toCharArray());
    }

    // E14: CREDENCIALES_INVALIDAS igual para correo inexistente, contrasena incorrecta
    // o cuenta inhabilitada
    @Transactional
    public SesionAbierta abrirSesion(String correo, char[] contrasena) {

        if (contrasena == null) {
            throw new CredencialesInvalidasException();
        }

        Optional<Cuenta> encontrada = cuentaRepository.buscarPorCorreo(correo);
        String resumen = encontrada.map(Cuenta::getResumenContrasena).orElse(resumenSinCuenta);
        boolean contrasenaValida = hashContrasenas.verificar(contrasena, resumen);

        if (encontrada.isEmpty() || !contrasenaValida || !encontrada.get().estaActiva()) {
            throw new CredencialesInvalidasException();
        }

        Cuenta cuenta = encontrada.get();
        Rol rolActivo = rolActivoDe(cuenta);
        String token = tokens.generar();

        sesionRepository.guardar(
                new Sesion(
                        null,
                        cuenta.getIdCuenta(),
                        tokens.resumirSiValido(token),
                        rolActivo,
                        Instant.now(),
                        null,
                        null));

        return new SesionAbierta(
                token,
                null,
                rolActivo.name(),
                matrizPermisos.permisosDe(rolActivo).stream().sorted().toList());
    }

    // Bearer: identidad de un token vigente de una cuenta activa
    @Transactional(readOnly = true)
    public Optional<IdentidadSesion> validar(String token) {

        String resumen = tokens.resumirSiValido(token);
        if (resumen == null) {
            return Optional.empty();
        }

        Instant ahora = Instant.now();
        return sesionRepository
                .buscarPorResumenToken(resumen)
                .filter(sesion -> sesion.estaVigente(ahora))
                .filter(sesion -> cuentaActiva(sesion.getIdCuenta()))
                .map(this::identidadDe);
    }

    // E15: el token deja de ser valido; las evaluaciones en curso no se tocan
    @Transactional
    public void cerrarSesion(String token) {

        String resumen = tokens.resumirSiValido(token);
        if (resumen == null || !sesionRepository.cerrar(resumen, Instant.now())) {
            throw new NoAutenticadoException();
        }
    }

    private Rol rolActivoDe(Cuenta cuenta) {
        Set<Rol> roles = cuenta.getRoles();
        if (roles.size() != 1) {
            throw new RolActivoNoDeterminadoException();
        }
        return roles.iterator().next();
    }

    private boolean cuentaActiva(Long idCuenta) {
        return cuentaRepository.buscarPorId(idCuenta).map(Cuenta::estaActiva).orElse(false);
    }

    private IdentidadSesion identidadDe(Sesion sesion) {
        return new IdentidadSesion(
                sesion.getIdCuenta(),
                sesion.getIdSesion(),
                sesion.getRolActivo().name(),
                matrizPermisos.permisosDe(sesion.getRolActivo()));
    }
}
