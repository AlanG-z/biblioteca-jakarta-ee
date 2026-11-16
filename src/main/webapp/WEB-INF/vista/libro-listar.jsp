<%-- ============================================================
     Listado de libros (administrador).
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


    <!-- Listado -->
    <section class="seccion" style="margin-top: 1rem;">
        <div class="seccion-cabecera">
            <h2>Libros</h2>
            <a class="btn btn-primario"
               href="${pageContext.request.contextPath}/libro/nuevo">+ Nuevo libro</a>
        </div>

        <!-- Mismo buscador del catálogo, apuntando a este listado -->
        <c:set var="accionBuscador"
               value="${pageContext.request.contextPath}/libro/listar"/>
        <%@ include file="/WEB-INF/vista/buscador-libros.jspf" %>

        <!-- Resultados: bloque que reemplaza la búsqueda en vivo -->
        <div data-busqueda="resultados">
        <!-- Paginación -->
        <c:set var="pagina" value="${pagina}"/>
        <%@ include file="/WEB-INF/vista/paginacion.jspf" %>

        <c:choose>
            <c:when test="${empty libros}">
                <div class="vacio">
                    <p>
                        <c:choose>
                            <c:when test="${not empty q or not empty categoriaSel}">
                                No se encontraron libros con ese criterio.
                            </c:when>
                            <c:otherwise>No hay libros en el catálogo.</c:otherwise>
                        </c:choose>
                    </p>
                    <a class="btn" href="${pageContext.request.contextPath}/libro/nuevo">Cargar el primero</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Título</th>
                                <th>Autor</th>
                                <th>Categoria</th>
                                <th>Ejemplares Disponibles</th>
                                <th>Totalde Ejemplares</th>
                                <th class="acciones">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="l" items="${libros}">
                                <tr>
                                    <td><c:out value="${l.id}"/></td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/libro/detalle?id=${l.id}">
                                            <c:out value="${l.titulo}"/>
                                        </a>
                                    </td>
                                    <td><c:out value="${l.autor}"/></td>
                                     <td>
                                        <c:out value="${l.categoria.nombre}"/>
                                    </td>
                                    <td>
                                        <c:out value="${l.ejemplaresDisponibles}"/>
                                    </td>
                                     <td>
                                        <c:out value="${l.ejemplaresTotal}"/>
                                    </td>
                                    
                                    <td class="acciones">
                                        <a class="btn btn-secundario btn-chico" href="${pageContext.request.contextPath}/libro/editar?id=${l.id}">Editar</a>
                                        <form class="acciones" method="post" style="display:inline"
                                              action="${pageContext.request.contextPath}/libro/eliminar">
                                            <input type="hidden" name="id" value="${l.id}">
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
        </div>
    </section>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>