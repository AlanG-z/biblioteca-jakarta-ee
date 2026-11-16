<%-- ============================================================
     Detalle de un libro: datos completos y botón para pedir
     préstamo (solo con sesión iniciada).
     Atributos: libro, categorias, error.
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Detalle del libro"/>
<%@ include file="/WEB-INF/vista/cabecera.jspf" %>

<c:if test="${not empty error}">
    <div class="alerta alerta-error" role="alert">
        <c:out value="${error}"/>
    </div>
</c:if>

<c:choose>
    <c:when test="${empty libro}">
        <div class="vacio">
            <p>El libro solicitado no existe.</p>
            <a class="btn" href="${pageContext.request.contextPath}/catalogo">Volver al catálogo</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="pagina-cabecera">
            <h1><c:out value="${libro.titulo}"/></h1>
            <a class="btn btn-secundario"
               href="${pageContext.request.contextPath}/catalogo">← Volver al catálogo</a>
        </div>

        <div class="detalle">
            <div class="tarjeta">
                <dl class="datos-libro">
                    <div class="portada portada-grande">
    <c:choose>
        <c:when test="${not empty libro.portada}">
            <img src="<c:out value='${libro.portada}'/>"
                 alt="<c:out value='${libro.titulo}'/>"
                 onerror="this.replaceWith(Object.assign(document.createElement('span'),{className:'portada-sin-imagen',textContent:'Sin portada'}))">
        </c:when>
        <c:otherwise>
            <span class="portada-sin-imagen">Sin portada</span>
        </c:otherwise>
    </c:choose>
</div>
                    <dt>Título</dt>
                    <dd><c:out value="${libro.titulo}"/></dd>

                    <dt>Autor</dt>
                    <dd><c:out value="${libro.autor}"/></dd>

                    <dt>Categoría</dt>
                    <dd>
                        <a href="${pageContext.request.contextPath}/Categoria?codigoCat=${libro.categoria.id}">
                            <c:out value="${libro.categoria.nombre}"/>
                        </a>
                    </dd>

                    <dt>ISBN</dt>
                    <dd>
                        <c:choose>
                            <c:when test="${not empty libro.isbn}"><c:out value="${libro.isbn}"/></c:when>
                            <c:otherwise><span class="muted">Sin ISBN</span></c:otherwise>
                        </c:choose>
                    </dd>

                    <dt>Ejemplares</dt>
                    <dd>
                        <c:out value="${libro.ejemplaresDisponibles}"/> de
                        <c:out value="${libro.ejemplaresTotal}"/> disponibles
                    </dd>
                </dl>
            </div>

            <div class="tarjeta">
                <h2>¿Quieres pedirlo?</h2>
                <p class="muted chico">
                    El préstamo dura 7 días. Si devuelve tarde se genera una multa
                    de $1.50 por día de atraso.
                </p>

                <c:choose>
                    <c:when test="${empty sessionScope.usuario}">
                        <p class="muted">Inicie sesión para pedir un préstamo.</p>
                        <a class="btn"
                           href="${pageContext.request.contextPath}/login">Iniciar sesión</a>
                    </c:when>
                    <c:when test="${libro.ejemplaresDisponibles <= 0}">
                        <div class="alerta alerta-error" role="status">
                            No hay ejemplares disponibles ahora mismo.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <form class="formulario" method="post"
                              action="${pageContext.request.contextPath}/prestamo/pedir">
                            <input type="hidden" name="libroId" value="${libro.id}">
                            <button class="btn btn-exito" type="submit">Pedir préstamo</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/vista/pie.jspf" %>
