package com.mycompany.biblioteca.sesiones;

import com.mycompany.biblioteca.Configuracion;
import com.mycompany.biblioteca.Multa;
import com.mycompany.biblioteca.Pagina;
import com.mycompany.biblioteca.ReglaNegocioException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Facade de multas: consulta de pendientes y marca como pagadas.
 * La generación de multas vive en PrestamoFacade: al devolver con atraso
 * y, de forma automática, al detectar un préstamo vencido.
 */
@Stateless
public class MultaFacade extends AbstractFacade<Multa> {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    /** Se usa para que listar multas actualice los atrasos pendientes. */
    @EJB
    private PrestamoFacade prestamoFacade;

    public MultaFacade() {
        super(Multa.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /** Página de las multas todavía no pagadas (de más recientes a más antiguas). */
    public List<Multa> pendientes(int numero) {
        prestamoFacade.actualizarAtrasados();
        return em.createQuery(
                "SELECT m FROM Multa m WHERE m.pagada = false ORDER BY m.id DESC", Multa.class)
                .setFirstResult(Pagina.desplazamiento(numero))
                .setMaxResults(Configuracion.PAGINA_TAMANO)
                .getResultList();
    }

    /** Total de multas pendientes: marca el número de páginas del listado. */
    public long contarPendientes() {
        prestamoFacade.actualizarAtrasados();
        return em.createQuery(
                "SELECT COUNT(m) FROM Multa m WHERE m.pagada = false", Long.class)
                .getSingleResult();
    }

    /** Suma de las multas pendientes (resumen "total a cobrar"). */
    public BigDecimal sumaPendiente() {
        prestamoFacade.actualizarAtrasados();
        BigDecimal total = em.createQuery(
                "SELECT SUM(m.monto) FROM Multa m WHERE m.pagada = false",
                BigDecimal.class)
                .getSingleResult();
        return total == null ? BigDecimal.ZERO : total;
    }

    /** Página de las multas de un usuario concreto (para "Mis préstamos"). */
    public List<Multa> porUsuario(Integer idUsuario, int numero) {
        prestamoFacade.actualizarAtrasados();
        return em.createQuery(
                "SELECT m FROM Multa m WHERE m.prestamo.usuario.id = :idUsuario ORDER BY m.id DESC",
                Multa.class)
                .setParameter("idUsuario", idUsuario)
                .setFirstResult(Pagina.desplazamiento(numero))
                .setMaxResults(Configuracion.PAGINA_TAMANO)
                .getResultList();
    }

    /** Total de multas de un usuario (para paginar su listado). */
    public long contarPorUsuario(Integer idUsuario) {
        prestamoFacade.actualizarAtrasados();
        return em.createQuery(
                "SELECT COUNT(m) FROM Multa m WHERE m.prestamo.usuario.id = :idUsuario", Long.class)
                .setParameter("idUsuario", idUsuario)
                .getSingleResult();
    }

    /** Marca una multa como pagada en el día de hoy. */
    public void marcarPagada(Integer id) throws ReglaNegocioException {
        Multa multa = find(id);
        if (multa == null) {
            throw new ReglaNegocioException("La multa no existe.");
        }
        if (multa.isPagada()) {
            throw new ReglaNegocioException("La multa ya está pagada.");
        }
        multa.setPagada(true);
        multa.setFechaPago(LocalDate.now());
        edit(multa);
    }
}
