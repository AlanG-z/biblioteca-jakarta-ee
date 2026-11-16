package com.mycompany.biblioteca.sesiones;

import com.mycompany.biblioteca.Categoria;
import com.mycompany.biblioteca.Configuracion;
import com.mycompany.biblioteca.Libro;
import com.mycompany.biblioteca.Pagina;
import com.mycompany.biblioteca.ReglaNegocioException;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * Facade de libros: CRUD del catálogo y búsqueda para el catálogo público.
 */
@Stateless
public class LibroFacade extends AbstractFacade<Libro> {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public LibroFacade() {
        super(Libro.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Da de alta un libro. Los ejemplares disponibles arrancan iguales
     * a los totales (ningún ejemplar prestado todavía).
     */
    public void crear(Integer idCategoria, String titulo, String autor, String isbn,
            String portada, int ejemplaresTotal) throws ReglaNegocioException {
        Categoria categoria = em.find(Categoria.class, idCategoria);
        if (categoria == null) {
            throw new ReglaNegocioException("Debe seleccionar una categoría válida.");
        }
        String tTitulo = titulo == null ? "" : titulo.trim();
        String tAutor = autor == null ? "" : autor.trim();
        if (tTitulo.isEmpty()) {
            throw new ReglaNegocioException("El título del libro es obligatorio.");
        }
        if (tAutor.isEmpty()) {
            throw new ReglaNegocioException("El autor del libro es obligatorio.");
        }
        validarLongitudes(tTitulo, tAutor, isbn == null ? "" : isbn.trim());
        if (ejemplaresTotal < 1) {
            throw new ReglaNegocioException("Los ejemplares totales deben ser al menos 1.");
        }
        String tIsbn = isbn == null ? "" : isbn.trim();
        if (!tIsbn.isEmpty() && !tIsbn.matches(Configuracion.PATRON_ISBN)) {
            throw new ReglaNegocioException("El ISBN no es válido (use 10 o 13 dígitos).");
        }
        if (!tIsbn.isEmpty() && buscarPorIsbn(tIsbn) != null) {
            throw new ReglaNegocioException("Ya existe un libro con ese ISBN.");
        }

        Libro libro = new Libro();
        libro.setCategoria(categoria);
        libro.setTitulo(tTitulo);
        libro.setAutor(tAutor);
        libro.setIsbn(tIsbn.isEmpty() ? null : tIsbn);
        libro.setPortada(portada == null ? "" : portada.trim());
        libro.setEjemplaresTotal(ejemplaresTotal);
        libro.setEjemplaresDisponibles(ejemplaresTotal);
        create(libro);
    }

    /** Modifica los datos de un libro (sin tocar préstamos en curso). */
    public void actualizar(Integer id, Integer idCategoria, String titulo, String autor,
            String isbn, String portada, int ejemplaresTotal) throws ReglaNegocioException {
        Libro libro = find(id);
        if (libro == null) {
            throw new ReglaNegocioException("El libro no existe.");
        }
        Categoria categoria = em.find(Categoria.class, idCategoria);
        if (categoria == null) {
            throw new ReglaNegocioException("Debe seleccionar una categoría válida.");
        }
        String tTitulo = titulo == null ? "" : titulo.trim();
        String tAutor = autor == null ? "" : autor.trim();
        if (tTitulo.isEmpty()) {
            throw new ReglaNegocioException("El título del libro es obligatorio.");
        }
        if (tAutor.isEmpty()) {
            throw new ReglaNegocioException("El autor del libro es obligatorio.");
        }
        validarLongitudes(tTitulo, tAutor, isbn == null ? "" : isbn.trim());
        String tIsbn = isbn == null ? "" : isbn.trim();
        if (!tIsbn.isEmpty() && !tIsbn.matches(Configuracion.PATRON_ISBN)) {
            throw new ReglaNegocioException("El ISBN no es válido (use 10 o 13 dígitos).");
        }
        if (!tIsbn.isEmpty()) {
            Libro duplicado = buscarPorIsbn(tIsbn);
            if (duplicado != null && !duplicado.getId().equals(id)) {
                throw new ReglaNegocioException("Ya existe un libro con ese ISBN.");
            }
        }
        int prestados = libro.getEjemplaresTotal() - libro.getEjemplaresDisponibles();
        if (ejemplaresTotal < prestados) {
            throw new ReglaNegocioException(
                    "Los ejemplares totales no pueden ser menores que los prestados (" + prestados + ").");
        }

        libro.setCategoria(categoria);
        libro.setTitulo(tTitulo);
        libro.setAutor(tAutor);
        libro.setIsbn(tIsbn.isEmpty() ? null : tIsbn);
        libro.setPortada(portada == null ? "" : portada.trim());
        libro.setEjemplaresTotal(ejemplaresTotal);
        // Si se redujo el total, se ajustan los disponibles (siempre >= 0)
        if (libro.getEjemplaresDisponibles() > ejemplaresTotal - prestados) {
            libro.setEjemplaresDisponibles(ejemplaresTotal - prestados);
        }
        edit(libro);
    }

    /**
     * Capa de negocio de longitud: evita el error 500 que produciría
     * exceder el tamaño de la columna en la base de datos.
     */
    private void validarLongitudes(String titulo, String autor, String isbn)
            throws ReglaNegocioException {
        if (titulo.length() > Configuracion.TITULO_MAXIMO) {
            throw new ReglaNegocioException("El título no puede superar "
                    + Configuracion.TITULO_MAXIMO + " caracteres.");
        }
        if (autor.length() > Configuracion.AUTOR_MAXIMO) {
            throw new ReglaNegocioException("El autor no puede superar "
                    + Configuracion.AUTOR_MAXIMO + " caracteres.");
        }
        if (isbn.length() > Configuracion.ISBN_MAXIMO) {
            throw new ReglaNegocioException("El ISBN no puede superar "
                    + Configuracion.ISBN_MAXIMO + " caracteres.");
        }
    }

    /**
     * Elimina el libro. Regla de negocio: no se puede eliminar si tiene
     * préstamos asociados; primero hay que resolverlos.
     */    public void eliminar(Integer id) throws ReglaNegocioException {
        Libro libro = find(id);
        if (libro == null) {
            throw new ReglaNegocioException("El libro no existe.");
        }
        Long prestamos = em.createQuery(
                "SELECT COUNT(p) FROM Prestamo p WHERE p.libro.id = :idLibro", Long.class)
                .setParameter("idLibro", id)
                .getSingleResult();
        if (prestamos > 0) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el libro: tiene " + prestamos
                    + " préstamo(s) asociado(s). Resuelva primero esos préstamos.");
        }
        remove(libro);
    }

    /**
     * Búsqueda paginada para el catálogo: por título o autor (parcial, sin
     * distinguir mayúsculas) y opcionalmente filtrada por categoría.
     */
    public List<Libro> buscar(String texto, Integer idCategoria, int numero) {
        StringBuilder jpql = new StringBuilder(
                "SELECT l FROM Libro l WHERE (LOWER(l.titulo) LIKE :t OR LOWER(l.autor) LIKE :t)");
        if (idCategoria != null) {
            jpql.append(" AND l.categoria.id = :idCat");
        }
        jpql.append(" ORDER BY l.titulo");

        var consulta = em.createQuery(jpql.toString(), Libro.class)
                .setParameter("t", patronBusqueda(texto));
        if (idCategoria != null) {
            consulta.setParameter("idCat", idCategoria);
        }
        return consulta
                .setFirstResult(Pagina.desplazamiento(numero))
                .setMaxResults(Configuracion.PAGINA_TAMANO)
                .getResultList();
    }

    /** Total de libros de esa misma búsqueda: fija el número de páginas. */
    public long contarBuscar(String texto, Integer idCategoria) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(l) FROM Libro l WHERE (LOWER(l.titulo) LIKE :t OR LOWER(l.autor) LIKE :t)");
        if (idCategoria != null) {
            jpql.append(" AND l.categoria.id = :idCat");
        }
        var consulta = em.createQuery(jpql.toString(), Long.class)
                .setParameter("t", patronBusqueda(texto));
        if (idCategoria != null) {
            consulta.setParameter("idCat", idCategoria);
        }
        return consulta.getSingleResult();
    }

    /** Patrón LIKE de la búsqueda (minúsculas, comodines en los extremos). */
    private String patronBusqueda(String texto) {
        return "%" + (texto == null ? "" : texto.trim().toLowerCase()) + "%";
    }

    /** Página del listado del administrador (ordenado por título). */
    public List<Libro> pagina(int numero) {
        return em.createQuery("SELECT l FROM Libro l ORDER BY l.titulo", Libro.class)
                .setFirstResult(Pagina.desplazamiento(numero))
                .setMaxResults(Configuracion.PAGINA_TAMANO)
                .getResultList();
    }

    /** Total de libros: marca el número de páginas del listado. */
    public long contar() {
        return em.createQuery("SELECT COUNT(l) FROM Libro l", Long.class)
                .getSingleResult();
    }

    /** Suma de ejemplares disponibles de todo el catálogo (resumen). */
    public long sumaEjemplaresDisponibles() {
        Long total = em.createQuery(
                "SELECT SUM(l.ejemplaresDisponibles) FROM Libro l", Long.class)
                .getSingleResult();
        return total == null ? 0 : total;
    }

    /** Libros de una categoría concreta (ruta /Categoria). */
    public List<Libro> porCategoria(Integer idCategoria) {
        return em.createQuery(
                "SELECT l FROM Libro l WHERE l.categoria.id = :idCat ORDER BY l.titulo", Libro.class)
                .setParameter("idCat", idCategoria)
                .getResultList();
    }

    /** Busca un libro por ISBN (para evitar duplicados). */
    public Libro buscarPorIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }
        List<Libro> resultados = em.createQuery(
                "SELECT l FROM Libro l WHERE l.isbn = :isbn", Libro.class)
                .setParameter("isbn", isbn.trim())
                .getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }
}
