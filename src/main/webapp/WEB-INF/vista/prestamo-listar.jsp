<%-- ============================================================
     Listado de préstamos (administrador).
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
            <h2>Préstamos</h2>
        </div>

        <!-- Búsqueda por la persona que recibió el préstamo -->
        <form class="buscador" method="get"
              action="${pageContext.request.contextPath}/prestamo/listar"
              role="search" aria-label="Buscar préstamos">
            <div class="campo campo-busqueda">
                <label for="q">Persona o libro</label>
                <input type="search" id="q" name="q" class="input"
                       maxlength="150"
                       value="<c:out value='${q}'/>"
                       placeholder="Nombre, email o título del libro">
            </div>
            <div class="buscador-acciones">
                <button class="btn btn-primario" type="submit">Buscar</button>
                <c:if test="${not empty q}">
                    <a class="btn btn-secundario"
                       href="${pageContext.request.contextPath}/prestamo/listar">Limpiar</a>
                </c:if>
            </div>
        </form>

        <!-- El contador siempre está en el HTML (oculto si no hay
             filtro) para que la búsqueda en vivo pueda actualizarlo -->
        <p class="buscador-resultados" data-busqueda="contador" role="status"
           <c:if test="${empty q}">hidden</c:if>>
            <c:choose>
                <c:when test="${pagina.totalRegistros == 1}">1 préstamo encontrado</c:when>
                <c:otherwise>
                    <c:out value="${pagina.totalRegistros}"/> préstamos encontrados
                </c:otherwise>
            </c:choose>
            <c:if test="${not empty q}">
                &middot; persona: &laquo;<c:out value="${q}"/>&raquo;
            </c:if>
        </p>

        <!-- Resultados: bloque que reemplaza la búsqueda en vivo -->
        <div data-busqueda="resultados">
        <!-- Paginación -->
    

        <c:choose>
            <c:when test="${empty prestamos}">
                <div class="vacio">
                    <p>
                        <c:choose>
                            <c:when test="${not empty q}">
                                No se encontraron préstamos para &laquo;<c:out value="${q}"/>&raquo;.
                            </c:when>
                            <c:otherwise>No hay préstamos registrados.</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Usuario</th>
                                <th>Libro</th>
                                <th>Fecha préstamo</th>
                                <th>Devolver antes</th>
                                <th>Estado</th>
                                <th class="acciones">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${prestamos}">
                                <tr>
                                    <td><c:out value="${p.id}"/></td>
                                    <td>
                                        <c:out value="${p.usuario.nombre}"/>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/libro/detalle?id=${p.libro.id}">
                                            <c:out value="${p.libro.titulo}"/>
                                        </a>
                                    </td>
                                    <td><c:out value="${p.fechaPrestamo}"/></td>
                                    <td><c:out value="${p.fechaDevolucionPrevista}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.estado == 'ACTIVO'}">
                                                <span class="etiqueta etiqueta-activo">Activo</span>
                                            </c:when>
                                            <c:when test="${p.estado == 'ATRASADO'}">
                                                <span class="etiqueta etiqueta-atrasado">Atrasado</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="etiqueta etiqueta-devuelto">Devuelto</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="acciones">
                                        <c:choose>
                                            <c:when test="${p.estado=='ACTIVO'|| p.estado=='ATRASADO'}">
                                                
                                  
                                        <a class="btn btn-secundario btn-chico" href="${pageContext.request.contextPath}/prestamo/editar?id=${p.id}">Editar</a>
                                        <form class="acciones" method="post" style="display:inline"
                                              action="${pageContext.request.contextPath}/prestamo/devolver">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <button class="btn btn-peligro btn-chico" type="submit">Devolver</button>
                                        </form>
                                                </c:when>
                                        <c:otherwise><button class="btn btn-secundario btn-chico"> - </button></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                
            </c:otherwise>
            
        </c:choose>
            <c:set var="pagina" value="${pagina}"/>
        <%@ include file="/WEB-INF/vista/paginacion.jspf" %>
        </div>
    </section>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>