package com.mycompany.biblioteca;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de una consulta paginada: los elementos de la página actual
 * más todos los datos que necesita la vista para dibujar los controles
 * (fragmento paginacion.jspf).
 *
 * @param <T> tipo de los elementos de la página
 */
public class Pagina<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Elementos de la página actual (nunca vacía si hay registros). */
    private final List<T> elementos;

    /** Número de la página actual (1 en adelante). */
    private final int actual;

    /** Primera y última página que se muestran en los botones (ventana). */
    private final int paginaDesde;
    private final int paginaHasta;

    /** Cantidad total de páginas y de registros de la consulta. */
    private final int totalPaginas;
    private final long totalRegistros;

    /** Registros por página. */
    private final int tamano;

    /** Ruta base de los enlaces (sin la query). */
    private final String rutaPagina;

    /** Parámetros adicionales a conservar en cada enlace (buscador, filtro). */
    private final String parametros;

    private Pagina(List<T> elementos, int actual, int paginaDesde, int paginaHasta,
            int totalPaginas, long totalRegistros, int tamano,
            String rutaPagina, String parametros) {
        this.elementos = elementos == null ? Collections.emptyList() : elementos;
        this.actual = actual;
        this.paginaDesde = paginaDesde;
        this.paginaHasta = paginaHasta;
        this.totalPaginas = totalPaginas;
        this.totalRegistros = totalRegistros;
        this.tamano = tamano;
        this.rutaPagina = rutaPagina == null ? "" : rutaPagina;
        this.parametros = parametros == null ? "" : parametros;
    }

    /** Total de páginas que corresponden a esa cantidad de registros. */
    public static int totalPaginas(long totalRegistros) {
        int tamano = Configuracion.PAGINA_TAMANO;
        return (int) Math.max(1, (totalRegistros + tamano - 1) / tamano);
    }

    /**
     * Ajusta la página pedida en la URL al rango válido (1..totalPaginas):
     * un valor ausente o negativo da 1 y uno fuera de rango da la última.
     */
    public static int ajustar(Integer pedida, long totalRegistros) {
        if (pedida == null || pedida < 1) {
            return 1;
        }
        return Math.min(pedida, totalPaginas(totalRegistros));
    }

    /** Desplazamiento JPQL que corresponde a una página (1-based). */
    public static int desplazamiento(int pagina) {
        return (Math.max(1, pagina) - 1) * Configuracion.PAGINA_TAMANO;
    }

    /** Construye la página a partir de los elementos ya recortados. */
    public static <T> Pagina<T> de(List<T> elementos, long totalRegistros, int actual,
            String rutaPagina, String parametros) {
        int paginas = totalPaginas(totalRegistros);
        int tamano = Configuracion.PAGINA_TAMANO;
        int desde = (Math.max(1, actual) - 1) * tamano + 1;
        int hasta = (int) Math.min((long) Math.max(1, actual) * tamano, totalRegistros);
        // Ventana de botones: hasta 5 páginas, centradas en la actual
        int paginaDesde = Math.max(1, actual - 2);
        int paginaHasta = Math.min(paginas, paginaDesde + 4);
        paginaDesde = Math.max(1, paginaHasta - 4);
        return new Pagina<>(elementos, actual, paginaDesde, paginaHasta,
                paginas, totalRegistros, tamano, rutaPagina, parametros);
    }

    public List<T> getElementos() {
        return elementos;
    }

    public int getActual() {
        return actual;
    }

    public int getPaginaDesde() {
        return paginaDesde;
    }

    public int getPaginaHasta() {
        return paginaHasta;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public long getTotalRegistros() {
        return totalRegistros;
    }

    public int getTamano() {
        return tamano;
    }

    public String getRutaPagina() {
        return rutaPagina;
    }

    public String getParametros() {
        return parametros;
    }

    /** Primer elemento visible de esta página (para "Mostrando X–Y de Z"). */
    public long getItemDesde() {
        return totalRegistros == 0 ? 0 : (long) (actual - 1) * tamano + 1;
    }

    /** Último elemento visible de esta página. */
    public long getItemHasta() {
        return totalRegistros == 0 ? 0
                : Math.min((long) actual * tamano, totalRegistros);
    }
}
