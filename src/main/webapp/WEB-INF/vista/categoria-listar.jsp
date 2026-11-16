<%-- ============================================================
     Listado de categorías (administrador).
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
            <span class="etiqueta-dato">Categorías registradas</span>
        </div>
   
    </div>

    <!-- Listado -->
    <section class="seccion" style="margin-top: 1rem;">
        <div class="seccion-cabecera">
            <h2>Categorías</h2>
            <a class="btn btn-primario"
               href="${pageContext.request.contextPath}/categoria/nuevo">+ Nueva categoría</a>
        </div>
        <!-- Paginación -->
        <c:set var="pagina" value="${pagina}"/>
        <%@ include file="/WEB-INF/vista/paginacion.jspf" %>

        <c:choose>
            <c:when test="${empty categorias}">
                <div class="vacio">
                    <p>No hay categorías cargadas.</p>
                    <a class="btn" href="${pageContext.request.contextPath}/categoria/nuevo">Crear la primera</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nombre</th>
                                <th class="acciones">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="c" items="${categorias}">
                                <tr>
                                    <td><c:out value="${c.id}"/></td>
                                    <td><c:out value="${c.nombre}"/></td>
                                    <td class="acciones">
                                        <a class="btn btn-secundario btn-chico" href="${pageContext.request.contextPath}/categoria/editar?id=${c.id}">Editar</a>
                                        <form class="acciones" method="post" style="display:inline"
                                              action="${pageContext.request.contextPath}/categoria/eliminar">
                                            <input type="hidden" name="id" value="${c.id}">
                                            <button class="btn btn-peligro btn-chico" type="submit">Eliminar</button>
                                        </form>
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