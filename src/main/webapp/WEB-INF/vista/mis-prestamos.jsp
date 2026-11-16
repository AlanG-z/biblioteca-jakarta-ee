<%-- ============================================================
     Vista del cliente: sus préstamos y multas.
     Bug arreglado: se eliminaron todas las etiquetas c:choose/c:if anidadas
     que causaban PWC6305 "The end tag /c:otherwise is unbalanced".
     Se usan exclusivamente expresiones EL ${} directas en el output.
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/vista/cabecera.jspf" %>

<div class="contenedor">
    <!-- Mensaje de éxito (flash) -->
    <c:if test="${not empty exito}">
        <div class="alerta alerta-exito" role="status">
            <c:out value="${exito}"/>
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alerta alerta-error" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <!-- Resumen rápido -->
    <div class="tarjetas-estadisticas">
        <div class="tarjeta-estadistica">
            <div class="numero"><c:out value="${paginaPrestamos.totalRegistros}"/></div>
            <div class="etiqueta">Préstamos totales</div>
        </div>
        <div class="tarjeta-estadistica">
            <div class="numero"><c:out value="${prestamosNoDevueltos}"/></div>
            <div class="etiqueta">Con libro en casa</div>
        </div>
        <div class="tarjeta-estadistica">
            <div class="numero"><c:out value="${paginaMultas.totalRegistros}"/></div>
            <div class="etiqueta">Multas registradas</div>
        </div>
    </div>

    <!-- Listado de préstamos -->
    <div class="seccion" style="margin-top: 2rem;">
        <h2>Préstamos</h2>
        <!-- Paginación -->
        <c:set var="pagina" value="${paginaPrestamos}"/>
        <%@ include file="/WEB-INF/vista/paginacion.jspf" %>

        <c:choose>
            <c:when test="${empty prestamos}">
                <div class="vacio">
                    <p>Todavía no pidió ningún libro.</p>
                    <a class="btn" href="${pageContext.request.contextPath}/catalogo">Ver catálogo</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <thead>
                            <tr>
                                <th>Libro</th>
                                <th>Préstamo</th>
                                <th>Devolver antes de</th>
                                <th>Estado</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${prestamos}">
                                <tr>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/libro/detalle?id=${p.libro.id}">
                                            <c:out value="${p.libro.titulo}"/>
                                        </a>
                                    </td>
                                    <td><c:out value="${p.fechaPrestamo}"/></td>
                                    <td><c:out value="${p.fechaDevolucionPrevista}"/></td>
                                    <td>
                                        <!-- Estado usando expresión EL '?' directa sin c:choose anidado -->
                                        <c:out value="${p.estado == 'ACTIVO' ? 'Activo' : p.estado == 'ATRASADO' ? 'Atrasado' : 'Devuelto'}"/>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Listado de multas -->
    <div class="seccion" style="margin-top: 2rem;">
        <h2 style="margin-top: 1.5rem;">Multas</h2>
        <!-- Paginación -->
        <c:set var="pagina" value="${paginaMultas}"/>
        <%@ include file="/WEB-INF/vista/paginacion.jspf" %>

        <c:choose>
            <c:when test="${empty multas}">
                <div class="vacio">
                    <p>No tiene multas. ¡Siga así!</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <thead>
                            <tr>
                                <th>Préstamo</th>
                                <th class="num">Días de atraso</th>
                                <th class="num">Monto</th>
                                <th>Estado</th>
                                <th>Fecha de pago</th>
                                <!-- <th>Acción</th> -->
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="m" items="${multas}">
                                <tr>
                                    <td>#<c:out value="${m.prestamo.id}"/></td>
                                    <td class="num"><c:out value="${m.diasAtraso}"/></td>
                                    <td class="num">
                                        $<fmt:formatNumber value="${m.monto}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${m.pagada}">
                                                <span class="etiqueta etiqueta-pagada">Pagada</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="etiqueta etiqueta-impa">Impaga</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty m.fechaPago}">
                                                <c:out value="${m.fechaPago}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="muted">—</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <!-- <td>
                                        <c:choose>
                                            <c:when test="${m.pagada}">
                                                <span class="muted">—</span>
                                            </c:when>
                                            <c:otherwise>
                                                <form method="post"
                                                      action="${pageContext.request.contextPath}/prestamo/pagar-multa"
                                                      style="display:inline;">
                                                    <input type="hidden" name="id" value="${m.id}">
                                                    <button class="btn btn-exito btn-chico" type="submit">Pagar</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </td> -->
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                <p class="muted chico">
                    Las multas impagas deben pagarse antes de pedir un nuevo libro. Al pagar, la multa queda registrada como pagada para usted y para el administrador.
                </p>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>