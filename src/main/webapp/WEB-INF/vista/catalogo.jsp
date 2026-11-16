<%-- ============================================================
     Catálogo de libros – grilla de tarjetas.
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ include file="/WEB-INF/vista/cabecera.jspf" %>

<div class="contenedor">
    <!-- Resumen (se actualiza junto con la búsqueda en vivo) -->
    <div class="resumen" data-busqueda="resumen">
        <div class="dato">
           
            <span class="etiqueta-dato">Bienvenido a BibliotecaVirtual </span>
        </div>
    </div>

    <!-- Grilla de tarjetas -->
    <section class="seccion">
        <h2>Catálogo de libros</h2>

        <!-- Buscador: título/autor + categoría -->
        <%@ include file="/WEB-INF/vista/buscador-libros.jspf" %>

        <!-- Resultados: es el bloque que reemplaza la búsqueda en vivo -->
        <div data-busqueda="resultados">
            <c:if test="${empty libros}">
                <div class="vacio">
                    <p>No se encontraron libros con ese criterio de búsqueda.</p>
                    <a class="btn btn-secundario"
                       href="${pageContext.request.contextPath}/catalogo">Ver todo el catálogo</a>
                </div>
            </c:if>

            <c:if test="${not empty libros}">
            <div class="grid-libros">
            <c:forEach var="libro" items="${libros}">
                <article class="card-libro">
                    <a href="${pageContext.request.contextPath}/libro/detalle?id=${libro.id}" class="tarjeta">
                        <img class="portada-libro"
                             src="${libro.portada}"
                             alt="Portada: <c:out value="${libro.titulo}"/>">
                        <div class="card-libro-contenido">
                            <h3 class="titulo-libro"><c:out value="${libro.titulo}"/></h3>
                            <p class="autor-libro"><c:out value="${libro.autor}"/></p>
                            <p class="estado-disponibilidad">
                                <c:choose>
                                    <c:when test="${libro.ejemplaresDisponibles > 0}">
                                        <span class="badge badge-activo">Disponible</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-impa">Sin stock</span>
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                        <div class="acciones">
                            <button class="btn btn-pedir" type="button">Pedir préstamo</button>
                        </div>
                    </a>
                </article>
            </c:forEach>
        </div>
        </c:if>

            <!-- Paginación dentro del bloque de resultados -->
            <%@ include file="/WEB-INF/vista/paginacion.jspf" %>
        </div>
    </section>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>