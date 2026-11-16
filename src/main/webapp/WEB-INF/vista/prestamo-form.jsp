
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Prórroga de préstamo"/>
<%@ include file="/WEB-INF/vista/cabecera.jspf" %>

<div class="pagina-cabecera">
    <h1 >Prórroga del prestamo #<c:out value="${prestamo.id}"/></h1>
    <a class="btn btn-secundario"
       href="${pageContext.request.contextPath}/prestamo/listar">← Volver</a>
</div>

<div class="tarjeta" style="max-width:900px; margin-left:auto; margin-right:auto;">
    <c:if test="${not empty error}">
        <div class="alerta alerta-error" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>



        <%-- ---------- Prórroga: solo cambia la fecha prevista ---------- --%>
        <c:choose>
        <c:when test="${not empty prestamo}">
            <p class="muted">
                Libro: <strong><c:out value="${prestamo.libro.titulo}"/></strong> ·
                Usuario: <strong><c:out value="${prestamo.usuario.nombre}"/></strong><br>
                Préstamo: <c:out value="${prestamo.fechaPrestamo}"/>
            </p>

            <form class="formulario" method="post"
                  action="${pageContext.request.contextPath}/prestamo/guardar">
                <input type="hidden" name="id" value="${prestamo.id}">

                <div class="campo">
                    <label for="fechaDevolucionPrevista">Nueva fecha de devolución prevista</label>
                    <input type="date" id="fechaDevolucionPrevista"
                           name="fechaDevolucionPrevista" required
                           min="${prestamo.fechaPrestamo}"
                           title="No puede ser anterior a la fecha del préstamo"
                           value="${prestamo.fechaDevolucionPrevista}">
                    <span class="ayuda">
                        El préstamo dura 7 días desde la fecha del préstamo.
                    </span>
                </div>

                <div class="acciones">
                    <button class="btn" type="submit">Guardar prórroga</button>
                    <a class="btn btn-secundario"
                       href="${pageContext.request.contextPath}/prestamo/listar">Cancelar</a>
                </div>
            </form>
        </c:when>
        <c:otherwise>
            <p class="muted">Préstamo no encontrado.</p>
        </c:otherwise>
        </c:choose>
    </div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>
