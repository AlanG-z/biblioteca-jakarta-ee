package com.mycompany.biblioteca.sesiones;

import com.mycompany.biblioteca.Categoria;
import com.mycompany.biblioteca.Configuracion;
import com.mycompany.biblioteca.Pagina;
import com.mycompany.biblioteca.ReglaNegocioException;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 * Facade de categorías. Además del CRUD, controla que no se elimine
 * una categoría que todavía tiene libros asociados.
 */
@Stateless
public class CategoriaFacade extends AbstractFacade<Categoria> {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public CategoriaFacade() {
        super(Categoria.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /** Da de alta una categoría validando el nombre. */
    public void crear(String nombre) throws ReglaNegocioException {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            throw new ReglaNegocioException("El nombre de la categoría es obligatorio.");
        }
        if (limpio.length() > Configuracion.NOMBRE_MAXIMO) {
            throw new ReglaNegocioException("El nombre no puede superar los "
                    + Configuracion.NOMBRE_MAXIMO + " caracteres.");
        }
        if (buscarPorNombre(limpio) != null) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre.");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(limpio);
        create(categoria);
    }

    /** Modifica el nombre de una categoría. */
    public void actualizar(Integer id, String nombre) throws ReglaNegocioException {
        Categoria categoria = find(id);
        if (categoria == null) {
            throw new ReglaNegocioException("La categoría no existe.");
        }
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            throw new ReglaNegocioException("El nombre de la categoría es obligatorio.");
        }
        if (limpio.length() > Configuracion.NOMBRE_MAXIMO) {
            throw new ReglaNegocioException("El nombre no puede superar los "
                    + Configuracion.NOMBRE_MAXIMO + " caracteres.");
        }
        Categoria duplicada = buscarPorNombre(limpio);
        if (duplicada != null && !duplicada.getId().equals(id)) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre.");
        }
        categoria.setNombre(limpio);
        edit(categoria);
    }

    /**
     * Elimina la categoría. Regla de negocio: no se puede eliminar si
     * tiene libros asociados; primero hay que darlos de baja.
     */
    public void eliminar(Integer id) throws ReglaNegocioException {
        Categoria categoria = find(id);
        if (categoria == null) {
            throw new ReglaNegocioException("La categoría no existe.");
        }
        Long libros = em.createQuery(
                "SELECT COUNT(l) FROM Libro l WHERE l.categoria.id = :idCat", Long.class)
                .setParameter("idCat", id)
                .getSingleResult();
        if (libros > 0) {
            throw new ReglaNegocioException(
                    "No se puede eliminar la categoría: tiene " + libros
                    + " libro(s) asociado(s). Elimine primero esos libros.");
        }
        remove(categoria);
    }

    /** Busca una categoría por su nombre exacto (para evitar duplicados). */
    public Categoria buscarPorNombre(String nombre) {
        List<Categoria> resultados = em.createQuery(
                "SELECT c FROM Categoria c WHERE LOWER(c.nombre) = :nombre", Categoria.class)
                .setParameter("nombre", nombre.trim().toLowerCase())
                .getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    /** Página del listado de categorías (orden alfabético). */
    public List<Categoria> pagina(int numero) {
        return em.createQuery("SELECT c FROM Categoria c ORDER BY c.nombre", Categoria.class)
                .setFirstResult(Pagina.desplazamiento(numero))
                .setMaxResults(Configuracion.PAGINA_TAMANO)
                .getResultList();
    }

    /** Total de categorías: marca el número de páginas del listado. */
    public long contar() {
        return em.createQuery("SELECT COUNT(c) FROM Categoria c", Long.class)
                .getSingleResult();
    }
}
