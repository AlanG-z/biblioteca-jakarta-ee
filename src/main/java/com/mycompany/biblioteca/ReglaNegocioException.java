package com.mycompany.biblioteca;

import jakarta.ejb.ApplicationException;

/**
 * Error de negocio (stock agotado, multas impagas, registro duplicado, etc.).
 * El servlet la captura y muestra el mensaje en error.jsp o en el formulario.
 * rollback = true: si la regla falla, la transacción EJB se deshace.
 */
@ApplicationException(rollback = true)
public class ReglaNegocioException extends Exception {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
