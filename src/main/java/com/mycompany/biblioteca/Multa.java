package com.mycompany.biblioteca;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Multa por devolución tardía. Se genera en PrestamoFacade de forma
 * automática al detectar el atraso del préstamo y se ajusta al devolver.
 */
@Entity
@Table(name = "multas")
public class Multa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;

    @JoinColumn(name = "prestamo_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Prestamo prestamo;

    @Basic(optional = false)
    @NotNull
    @Column(name = "dias_atraso", nullable = false)
    private int diasAtraso;

    @Basic(optional = false)
    @NotNull
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Basic(optional = false)
    @NotNull
    @Column(name = "pagada", nullable = false)
    private boolean pagada;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    public Multa() {
    }

    public Multa(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    public int getDiasAtraso() {
        return diasAtraso;
    }

    public void setDiasAtraso(int diasAtraso) {
        this.diasAtraso = diasAtraso;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public boolean isPagada() {
        return pagada;
    }

    public void setPagada(boolean pagada) {
        this.pagada = pagada;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    @Override
    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public boolean equals(Object objeto) {
        if (!(objeto instanceof Multa)) {
            return false;
        }
        Multa otra = (Multa) objeto;
        return (this.id == null && otra.id == null)
                || (this.id != null && this.id.equals(otra.id));
    }

    @Override
    public String toString() {
        return "Multa[ id=" + id + " ]";
    }
}
