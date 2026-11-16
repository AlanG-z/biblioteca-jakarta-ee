package com.mycompany.biblioteca;

import java.math.BigDecimal;

/**
 * Constantes configurables del negocio.
 * Cambiar acá el préstamo en días o el monto diario de la multa.
 */
public final class Configuracion {

    /** Duración de un préstamo en días. */
    public static final int DIAS_PRESTAMO = 7;

    /** Monto de la multa por cada día de atraso. */
    public static final BigDecimal MONTO_MULTA_DIARIO = new BigDecimal("1.50");

    /** Longitud mínima de la contraseña. */
    public static final int PASSWORD_MINIMO = 6;

    /** Formato de email aceptado (validación básica). */
    public static final String PATRON_EMAIL = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

    /** ISBN-10 (9 dígitos + dígito verificador) o ISBN-13 (13 dígitos). */
    public static final String PATRON_ISBN = "^(\\d{9}[\\dXx]|\\d{13})$";

    // --------------------------------------------------------
    //  Longitudes máximas (columnas de la base de datos).
    //  Deben coincidir con los maxlength de los formularios.
    // --------------------------------------------------------

    /** Longitud máxima de nombres de usuario y de categorías. */
    public static final int NOMBRE_MAXIMO = 100;

    /** Longitud máxima del email. */
    public static final int EMAIL_MAXIMO = 150;

    /** Longitud máxima del título de un libro. */
    public static final int TITULO_MAXIMO = 200;

    /** Longitud máxima del autor de un libro. */
    public static final int AUTOR_MAXIMO = 150;

    /** Longitud máxima del ISBN (13 dígitos). */
    public static final int ISBN_MAXIMO = 13;

    /** Registros que se muestran por página en cada listado. */
    public static final int PAGINA_TAMANO = 10;

    private Configuracion() {
        // Clase de utilidades: no se instancia
    }
}
