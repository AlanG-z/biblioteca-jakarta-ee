<%-- ============================================================
     Listado de multas (administrador).
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

    <!-- Resumen -->
    <div class="resumen">
        <div class="dato">
            <span class="numero"><c:out value="${pagina.totalRegistros}"/></span>
            <span class="etiqueta-dato">Multas impagas</span>
        </div>
        <div class="dato">
            <span class="numero">
                $<fmt:formatNumber value="${totalCobrar}" minFractionDigits="2" maxFractionDigits="2"/>
            </span>
            <span class="etiqueta-dato">Total a cobrar</span>
        </div>
    </div>

    <!-- Listado -->
    <section class="seccion" style="margin-top: 1rem;">
        <h2>Multas</h2>
        <!-- Paginación -->
        <c:set var="pagina" value="${pagina}"/>
        <%@ include file="/WEB-INF/vista/paginacion.jspf" %>

        <c:choose>
            <c:when test="${empty multas}">
                <div class="vacio">
                    <p>No hay multas pendientes. ¡Excelente!</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Préstamo</th>
                                <th class="num">Días de atraso</th>
                                <th class="num">Monto</th>
                                <th>Estado</th>
                                <th>Fecha de pago</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="m" items="${multas}">
                                <tr>
                                    <td><c:out value="${m.id}"/></td>
                                    <td>#<c:out value="${m.prestamo.id}"/></td>
                                    <td class="num"><c:out value="${m.diasAtraso}"/></td>
                                    <td class="num">
                                        $<fmt:formatNumber value="${m.monto}" minFractionDigits="2"
                                                           maxFractionDigits="2"/>
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
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                <p class="muted chico">
                    Las multas impagas deben pagarse en la biblioteca antes de pedir un nuevo libro.
                </p>
            </c:otherwise>
        </c:choose>
    </section>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>