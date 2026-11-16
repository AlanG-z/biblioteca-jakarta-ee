package com.mycompany.biblioteca;

/**
 * Estados posibles de un préstamo.
 * ACTIVO    -> prestado, sin devolver y aún no vencido.
 * ATRASADO  -> sigue activo pero venció la fecha prevista.
 * DEVUELTO  -> se registró la devolución (con o sin multa).
 */
public enum EstadoPrestamo {
    ACTIVO,
    DEVUELTO,
    ATRASADO
}
