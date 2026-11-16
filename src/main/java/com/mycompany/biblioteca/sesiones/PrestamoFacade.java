package com.mycompany.biblioteca.sesiones;

import com.mycompany.biblioteca.Configuracion;
import com.mycompany.biblioteca.EstadoPrestamo;
import com.mycompany.biblioteca.Libro;
import com.mycompany.biblioteca.Multa;
import com.mycompany.biblioteca.Pagina;
import com.mycompany.biblioteca.Prestamo;
import com.mycompany.biblioteca.ReglaNegocioException;
import com.mycompany.biblioteca.Usuario;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Facade de préstamos: acá viven TODAS las reglas de negocio de
 * préstamos y multas (transacciones gestionadas por el contenedor).
 *
 * Reglas:
 *  - Un préstamo dura Configuracion.DIAS_PRESTAMO días.
 *  - Solo se presta si hay ejemplares disponibles (se descuenta 1).
 *  - Al devolver se devuelve 1 ejemplar.
 *  - Un cliente con multas impagas no puede pedir libros.
 *  - Devolver tarde genera multa: días de atraso × MONTO_MULTA_DIARIO.
 *  - Un préstamo activo vencido queda en estado ATRASADO.
 */
@Stateless
public class PrestamoFacade extends AbstractFacade<Prestamo> {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public PrestamoFacade() {
        super(Prestamo.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Registra un nuevo préstamo para un usuario.
     * Valida stock y multas impagas, descuenta un ejemplar y calcula
     * la fecha de devolución prevista (hoy + DIAS_PRESTAMO).
     */
    public Prestamo pedir(Integer idUsuario, Integer idLibro) throws ReglaNegocioException {
        Usuario usuario = em.find(Usuario.class, idUsuario);
        if (usuario == null) {
            throw new ReglaNegocioException("El usuario no existe.");
        }
        if (!usuario.isActivo()) {
            throw new ReglaNegocioException("La cuenta del usuario está desactivada.");
        }
        Libro libro = em.find(Libro.class, idLibro);
        if (libro == null) {
            throw new ReglaNegocioException("El libro no existe.");
        }
        if (libro.getEjemplaresDisponibles() <= 0) {
            throw new ReglaNegocioException("No hay ejemplares disponibles de este libro.");
        }
        if (tieneMultasImpagas(idUsuario)) {
            throw new ReglaNegocioException(
                    "No puede pedir un libro: tiene multas impagas. "
                    + "Pague primero las multas pendientes.");
        }

        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(hoy);
        prestamo.setFechaDevolucionPrevista(hoy.plusDays(Configuracion.DIAS_PRESTAMO));
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        create(prestamo);

        // Se descuenta un ejemplar disponible (nunca menos de 0)
        libro.setEjemplaresDisponibles(
                Math.max(0, libro.getEjemplaresDisponibles() - 1));
        em.merge(libro);

        return prestamo;
    }

    /**
     * Registra la devolución de un préstamo.
     *  - Suma 1 ejemplar al libro.
     *  - Si se devuelve tarde, genera o ajusta la multa (días × monto diario).
     *  - El préstamo pasa a DEVUELTO con la fecha real de hoy.
     */
    public void devolver(Integer idPrestamo) throws ReglaNegocioException {
        Prestamo prestamo = em.find(Prestamo.class, idPrestamo);
        if (prestamo == null) {
            throw new ReglaNegocioException("El préstamo no existe.");
        }
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new ReglaNegocioException("Este préstamo ya fue devuelto.");
        }

        LocalDate hoy = LocalDate.now();
        Libro libro = prestamo.getLibro();
        // Se devuelve el ejemplar (nunca más que el total del libro)
        libro.setEjemplaresDisponibles(
                Math.min(libro.getEjemplaresTotal(),
                        libro.getEjemplaresDisponibles() + 1));
        em.merge(libro);

        // Si llega tarde, se genera (o ajusta) la multa correspondiente
        registrarMultaAtraso(prestamo, hoy);

        prestamo.setFechaDevolucionReal(hoy);
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        edit(prestamo);
    }

    /**
     * Prórroga (solo administrador): cambia la fecha de devolución
     * prevista de un préstamo que todavía no se devolvió.
     * Si la nueva fecha es hoy o futura, el préstamo vuelve a ACTIVO;
     * si queda en el pasado, pasa a ATRASADO.
     */
    public void ajustarFechaDevolucion(Integer idPrestamo, LocalDate nuevaFecha)
            throws ReglaNegocioException {
        Prestamo prestamo = em.find(Prestamo.class, idPrestamo);
        if (prestamo == null) {
            throw new ReglaNegocioException("El préstamo no existe.");
        }
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new ReglaNegocioException(
                    "No se puede modificar un préstamo que ya fue devuelto.");
        }
        if (nuevaFecha == null) {
            throw new ReglaNegocioException("La fecha de devolución prevista es obligatoria.");
        }
        if (nuevaFecha.isBefore(prestamo.getFechaPrestamo())) {
            throw new ReglaNegocioException(
                    "La fecha prevista no puede ser anterior a la fecha del préstamo.");
        }
        LocalDate hoy = LocalDate.now();
        prestamo.setFechaDevolucionPrevista(nuevaFecha);
        prestamo.setEstado(nuevaFecha.isBefore(hoy)
                ? EstadoPrestamo.ATRASADO : EstadoPrestamo.ACTIVO);
        if (nuevaFecha.isBefore(hoy)) {
            // Sigue atrasado: se recalcula la multa con los días hasta hoy
            registrarMultaAtraso(prestamo, hoy);
        } else {
            // Dejó de estar atrasado: se cancela la multa pendiente
            cancelarMultaPendiente(prestamo);
        }
        edit(prestamo);
    }

    /**
     * Marca como ATRASADO todo préstamo no devuelto cuya fecha prevista
     * ya pasó y le genera la multa que corresponde. Se llama antes de
     * listar, de modo que las multas aparecen solas sin esperar a la
     * devolución del libro.
     */
    public void actualizarAtrasados() {
        LocalDate hoy = LocalDate.now();
        List<Prestamo> vencidos = em.createQuery(
                "SELECT p FROM Prestamo p WHERE p.estado <> :estado "
                + "AND p.fechaDevolucionPrevista < :hoy", Prestamo.class)
                .setParameter("estado", EstadoPrestamo.DEVUELTO)
                .setParameter("hoy", hoy)
                .getResultList();
        for (Prestamo prestamo : vencidos) {
            if (prestamo.getEstado() != EstadoPrestamo.ATRASADO) {
                prestamo.setEstado(EstadoPrestamo.ATRASADO);
                edit(prestamo);
            }
            registrarMultaAtraso(prestamo, hoy);
        }
    }

    /**
     * Genera o ajusta la multa por atraso de un préstamo hasta la fecha
     * indicada (fecha de detección del atraso o fecha de devolución).
     *
     * Reglas:
     *  - Solo aplica si la fecha supera la de devolución prevista.
     *  - Se cobra siempre la diferencia: días de atraso totales menos
     *    los días que ya están cubiertos por multas pagadas.
     *  - A lo sumo una multa pendiente por préstamo (se recalcula);
     *    si no la hay, se crea por los días debidos.
     */
    private void registrarMultaAtraso(Prestamo prestamo, LocalDate fecha) {
        LocalDate prevista = prestamo.getFechaDevolucionPrevista();
        if (prevista == null || !fecha.isAfter(prevista)) {
            return;                         // todavía no hay atraso
        }
        int diasTotales = (int) ChronoUnit.DAYS.between(prevista, fecha);
        if (diasTotales <= 0) {
            return;
        }

        List<Multa> multas = em.createQuery(
                "SELECT m FROM Multa m WHERE m.prestamo.id = :id ORDER BY m.id",
                Multa.class)
                .setParameter("id", prestamo.getId())
                .getResultList();

        // Días que ya están pagados: solo se cobra la diferencia
        int diasPagados = 0;
        for (Multa multa : multas) {
            if (multa.isPagada()) {
                diasPagados += multa.getDiasAtraso();
            }
        }
        int diasDebidos = diasTotales - diasPagados;
        if (diasDebidos <= 0) {
            return;                         // el atraso ya está cubierto
        }

        // Si ya había una multa pendiente, se recalcula con lo debido
        for (Multa multa : multas) {
            if (!multa.isPagada()) {
                multa.setDiasAtraso(diasDebidos);
                multa.setMonto(montoMulta(diasDebidos));
                em.merge(multa);            // edit() es de Prestamo, no de Multa
                return;
            }
        }

        // No había multa pendiente: se crea por los días debidos
        Multa nueva = new Multa();
        nueva.setPrestamo(prestamo);
        nueva.setDiasAtraso(diasDebidos);
        nueva.setMonto(montoMulta(diasDebidos));
        nueva.setPagada(false);
        em.persist(nueva);
    }

    /**
     * Elimina las multas todavía no pagadas de un préstamo: se usa cuando
     * una prórroga deja el préstamo sin atraso.
     */
    private void cancelarMultaPendiente(Prestamo prestamo) {
        List<Multa> pendientes = em.createQuery(
                "SELECT m FROM Multa m WHERE m.prestamo.id = :id AND m.pagada = false",
                Multa.class)
                .setParameter("id", prestamo.getId())
                .getResultList();
        for (Multa multa : pendientes) {
            em.remove(multa);
        }
    }

    /** Monto de la multa: días de atraso × MONTO_MULTA_DIARIO. */
    private BigDecimal montoMulta(int dias) {
        return Configuracion.MONTO_MULTA_DIARIO
                .multiply(BigDecimal.valueOf(dias));
    }

    /**
     * Página de los préstamos de un usuario (para "Mis préstamos").
     * También actualiza los atrasos: el conteo debe reflejar el mismo estado.
     */
    public List<Prestamo> porUsuario(Integer idUsuario, int numero) {
        actualizarAtrasados();
        return em.createQuery(
                "SELECT p FROM Prestamo p WHERE p.usuario.id = :idUsuario "
                + "ORDER BY p.id DESC", Prestamo.class)
                .setParameter("idUsuario", idUsuario)
                .setFirstResult(Pagina.desplazamiento(numero))
                .setMaxResults(Configuracion.PAGINA_TAMANO)
                .getResultList();
    }

    /** Total de préstamos de un usuario (para paginar su listado). */
    public long contarPorUsuario(Integer idUsuario) {
        actualizarAtrasados();
        return em.createQuery(
                "SELECT COUNT(p) FROM Prestamo p WHERE p.usuario.id = :idUsuario", Long.class)
                .setParameter("idUsuario", idUsuario)
                .getSingleResult();
    }

    /** Página de todos los préstamos (listado del administrador). */
    public List<Prestamo> pagina(int numero) {
        actualizarAtrasados();
        return em.createQuery(
                "SELECT p FROM Prestamo p ORDER BY p.id DESC", Prestamo.class)
                .setFirstResult(Pagina.desplazamiento(numero))
                .setMaxResults(Configuracion.PAGINA_TAMANO)
                .getResultList();
    }

    /**
     * Búsqueda paginada para el listado del administrador: por la persona
     * a la que se le prestó (nombre o email) o por el título del libro.
     * Sin texto devuelve todos, igual que pagina().
     */
    public List<Prestamo> buscar(String texto, int numero) {
        actualizarAtrasados();
        return em.createQuery(
                "SELECT p FROM Prestamo p WHERE LOWER(p.usuario.nombre) LIKE :t "
                + "OR LOWER(p.usuario.email) LIKE :t "
                + "OR LOWER(p.libro.titulo) LIKE :t ORDER BY p.id DESC", Prestamo.class)
                .setParameter("t", patronBusqueda(texto))
                .setFirstResult(Pagina.desplazamiento(numero))
                .setMaxResults(Configuracion.PAGINA_TAMANO)
                .getResultList();
    }

    /** Total de préstamos de esa misma búsqueda: fija el número de páginas. */
    public long contarBuscar(String texto) {
        actualizarAtrasados();
        Long total = em.createQuery(
                "SELECT COUNT(p) FROM Prestamo p WHERE LOWER(p.usuario.nombre) LIKE :t "
                + "OR LOWER(p.usuario.email) LIKE :t "
                + "OR LOWER(p.libro.titulo) LIKE :t", Long.class)
                .setParameter("t", patronBusqueda(texto))
                .getSingleResult();
        return total == null ? 0 : total;
    }

    /** Patrón LIKE de la búsqueda (minúsculas, comodines en los extremos). */
    private String patronBusqueda(String texto) {
        return "%" + (texto == null ? "" : texto.trim().toLowerCase()) + "%";
    }

    /** Total de préstamos: marca el número de páginas del listado. */
    public long contar() {
        actualizarAtrasados();
        return em.createQuery("SELECT COUNT(p) FROM Prestamo p", Long.class)
                .getSingleResult();
    }

    /** Préstamos en estado ATRASADO (resumen del listado del admin). */
    public long contarAtrasados() {
        actualizarAtrasados();
        Long total = em.createQuery(
                "SELECT COUNT(p) FROM Prestamo p WHERE p.estado = :estado", Long.class)
                .setParameter("estado", EstadoPrestamo.ATRASADO)
                .getSingleResult();
        return total == null ? 0 : total;
    }

    /** Préstamos de un usuario que todavía no se devolvieron (resumen). */
    public long contarNoDevueltosPorUsuario(Integer idUsuario) {
        actualizarAtrasados();
        Long total = em.createQuery(
                "SELECT COUNT(p) FROM Prestamo p WHERE p.usuario.id = :idUsuario "
                + "AND p.estado <> :estado", Long.class)
                .setParameter("idUsuario", idUsuario)
                .setParameter("estado", EstadoPrestamo.DEVUELTO)
                .getSingleResult();
        return total == null ? 0 : total;
    }

    /** Indica si el usuario tiene al menos una multa sin pagar. */
    public boolean tieneMultasImpagas(Integer idUsuario) {
        Long impagas = em.createQuery(
                "SELECT COUNT(m) FROM Multa m WHERE m.pagada = false "
                + "AND m.prestamo.usuario.id = :idUsuario", Long.class)
                .setParameter("idUsuario", idUsuario)
                .getSingleResult();
        return impagas > 0;
    }
}
