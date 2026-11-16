<%-- ============================================================
     Listado de usuarios (administrador).
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
            <span class="etiqueta-dato">Usuarios registrados</span>
        </div>
        <div class="dato">
            <span class="numero"><c:out value="${administradores}"/></span>
            <span class="etiqueta-dato">Administradores</span>
        </div>
    </div>

    <!-- Listado -->
    <section class="seccion" style="margin-top: 1rem;">
        <h2>Usuarios</h2>
        <!-- Paginación -->
        <c:set var="pagina" value="${pagina}"/>
        <%@ include file="/WEB-INF/vista/paginacion.jspf" %>

        <c:choose>
            <c:when test="${empty usuarios}">
                <div class="vacio">
                    <p>No hay usuarios registrados.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nombre</th>
                                <th>Email</th>
                                <th>Rol</th>
                                <th>Estado</th>
                                <th class="acciones">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="u" items="${usuarios}">
                                <tr>
                                    <td><c:out value="${u.id}"/></td>
                                    <td><c:out value="${u.nombre}"/></td>
                                    <td><c:out value="${u.email}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.rol == 'ADMIN'}">
                                                <span class="etiqueta etiqueta-admin">Admin</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="etiqueta">Cliente</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.activo}">
                                                <span class="etiqueta etiqueta-activo">Activo</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="etiqueta etiqueta-atrasado">Desactivado</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="acciones">
                                        <a class="btn btn-secundario btn-chico" href="${pageContext.request.contextPath}/usuario/editar?id=${u.id}">Editar</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>