package com.mycompany.biblioteca.sesiones;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Facade genérica: operaciones CRUD básicas sobre una entidad.
 * Cada facade concreta agrega sus consultas y reglas de negocio.
 * @param <T> entidad JPA
 */
public abstract class AbstractFacade<T> {

    private final Class<T> claseEntidad;

    public AbstractFacade(Class<T> claseEntidad) {
        this.claseEntidad = claseEntidad;
    }

    /** Cada facade inyecta su propio EntityManager (JTA). */
    protected abstract EntityManager getEntityManager();

    public void create(T entidad) {
        getEntityManager().persist(entidad);
    }

    public void edit(T entidad) {
        getEntityManager().merge(entidad);
    }

    public void remove(T entidad) {
        EntityManager em = getEntityManager();
        em.remove(em.merge(entidad));
    }

    /** Busca y borra en la misma transacción. */
    public void removeById(Object id) {
        T entidad = find(id);
        if (entidad != null) {
            remove(entidad);
        }
    }

    public T find(Object id) {
        return getEntityManager().find(claseEntidad, id);
    }

    public List<T> findAll() {
        return getEntityManager()
                .createQuery("SELECT e FROM " + claseEntidad.getSimpleName() + " e", claseEntidad)
                .getResultList();
    }
}
