package com.mycompany.biblioteca.filtro;

import com.mycompany.biblioteca.Rol;
import com.mycompany.biblioteca.Usuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Protege las rutas de administración: exige sesión iniciada Y rol ADMIN.
 * Si no cumple, redirige a /login.
 *
 * Nota: /libro/detalle, /prestamo/pedir y /Categoria quedan fuera de
 * este filtro (son públicos o de cliente); el resto se protege rama por rama.
 */
@WebFilter({"/categoria/*", "/usuario/*", "/multa/*",
            "/prestamo/listar", "/prestamo/editar",
            "/prestamo/guardar", "/prestamo/eliminar", "/prestamo/devolver",
            "/libro/listar", "/libro/nuevo",
            "/libro/editar", "/libro/guardar", "/libro/eliminar"})
public class AdminFiltro implements Filter {

    @Override
    public void init(FilterConfig filtro) throws ServletException {
        // No necesita configuración
    }

    @Override
    public void doFilter(ServletRequest requerimiento, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest peticion = (HttpServletRequest) requerimiento;
        HttpServletResponse respuestaHttp = (HttpServletResponse) respuesta;

        HttpSession sesion = peticion.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");

        if (usuario == null || usuario.getRol() != Rol.ADMIN) {
            // Sin sesión o sin rol ADMIN: se manda al login
            respuestaHttp.sendRedirect(
                    peticion.getContextPath() + "/login");
            return;
        }

        cadena.doFilter(requerimiento, respuesta);
    }

    @Override
    public void destroy() {
        // No hay recursos que liberar
    }
}
