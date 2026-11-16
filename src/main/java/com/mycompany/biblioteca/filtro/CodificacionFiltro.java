package com.mycompany.biblioteca.filtro;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Filtro que fuerza UTF-8 en todas las peticiones y respuestas
 * (acentos y ñ en los formularios y en los JSP).
 */
@WebFilter("/*")
public class CodificacionFiltro implements Filter {

    @Override
    public void init(FilterConfig filtro) throws ServletException {
        // No necesita configuración
    }

    @Override
    public void doFilter(ServletRequest requerimiento, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        requerimiento.setCharacterEncoding("UTF-8");
        respuesta.setCharacterEncoding("UTF-8");
        cadena.doFilter(requerimiento, respuesta);
    }

    @Override
    public void destroy() {
        // No hay recursos que liberar
    }
}
