package com.mycompany.biblioteca;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Préstamo de un libro a un usuario.
 * Las reglas de negocio (stock, multas, atrasos) viven en PrestamoFacade.
 */
@Entity
@Table(name = "prestamos")
public class Prestamo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;

    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;

    @JoinColumn(name = "libro_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Libro libro;

    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_devolucion_prevista", nullable = false)
    private LocalDate fechaDevolucionPrevista;

    @Column(name = "fecha_devolucion_real")
    private LocalDate fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    @NotNull
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPrestamo estado;

    /** Multas generadas por este préstamo (carga perezosa). */
    @OneToMany(mappedBy = "prestamo")
    private List<Multa> multas;

    public Prestamo() {
    }

    public Prestamo(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucionPrevista() {
        return fechaDevolucionPrevista;
    }

    public void setFechaDevolucionPrevista(LocalDate fechaDevolucionPrevista) {
        this.fechaDevolucionPrevista = fechaDevolucionPrevista;
    }

    public LocalDate getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(LocalDate fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public List<Multa> getMultas() {
        return multas;
    }

    public void setMultas(List<Multa> multas) {
        this.multas = multas;
    }

    @Override
    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public boolean equals(Object objeto) {
        if (!(objeto instanceof Prestamo)) {
            return false;
        }
        Prestamo otro = (Prestamo) objeto;
        return (this.id == null && otro.id == null)
                || (this.id != null && this.id.equals(otro.id));
    }

    @Override
    public String toString() {
        return "Prestamo[ id=" + id + " ]";
    }
}
