package com.mycompany.biblioteca;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Libro del catálogo. Controla los ejemplares disponibles.
 * Relación N-1 con Categoria y 1-N con Prestamo.
 */
@Entity
@Table(name = "libros")
public class Libro implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;

    @JoinColumn(name = "categoria_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Categoria categoria;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "autor", nullable = false, length = 150)
    private String autor;

    @Size(max = 20)
    @Column(name = "isbn", length = 20)
    private String isbn;

    /** URL o nombre del archivo de la portada (vacío si no hay). */
    @Size(max = 255)
    @Column(name = "portada", length = 255)
    private String portada;

    @Basic(optional = false)
    @NotNull
    @Column(name = "ejemplares_total", nullable = false)
    private int ejemplaresTotal;

    @Basic(optional = false)
    @NotNull
    @Column(name = "ejemplares_disponibles", nullable = false)
    private int ejemplaresDisponibles;

    /** Lo completa la base de datos (DEFAULT CURRENT_TIMESTAMP). */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Préstamos de este libro (carga perezosa). */
    @OneToMany(mappedBy = "libro")
    private List<Prestamo> prestamos;

    public Libro() {
    }

    public Libro(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPortada() {
        return portada;
    }

    public void setPortada(String portada) {
        this.portada = portada;
    }

    public int getEjemplaresTotal() {
        return ejemplaresTotal;
    }

    public void setEjemplaresTotal(int ejemplaresTotal) {
        this.ejemplaresTotal = ejemplaresTotal;
    }

    public int getEjemplaresDisponibles() {
        return ejemplaresDisponibles;
    }

    public void setEjemplaresDisponibles(int ejemplaresDisponibles) {
        this.ejemplaresDisponibles = ejemplaresDisponibles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Prestamo> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }

    @Override
    public int hashCode() {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public boolean equals(Object objeto) {
        if (!(objeto instanceof Libro)) {
            return false;
        }
        Libro otro = (Libro) objeto;
        return (this.id == null && otro.id == null)
                || (this.id != null && this.id.equals(otro.id));
    }

    @Override
    public String toString() {
        return "Libro[ id=" + id + " ]";
    }
}
