package com.mycompany.biblioteca.filtro;

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
 * Exige sesión iniciada para las rutas de cliente:
 *   /mis-prestamos y /prestamo/* (pedir préstamo).
 * Si no hay usuario en sesión, redirige a /login.
 */
@WebFilter({"/mis-prestamos", "/prestamo/*"})
public class AutenticacionFiltro implements Filter {

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

        if (usuario == null) {
            // Sin sesión: se manda al login
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
