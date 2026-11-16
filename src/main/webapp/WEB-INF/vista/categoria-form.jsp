<%-- ============================================================
     Formulario de categoría (alta y edición).
     Atributos: categoria (opcional, para editar), error, nombre.
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="${empty categoria ? 'Nueva categoría' : 'Editar categoría'}"/>
<%@ include file="/WEB-INF/vista/cabecera.jspf" %>

<div class="pagina-cabecera">
    <h1>
        <c:choose>
            <c:when test="${empty categoria}">Nueva categoría</c:when>
            <c:otherwise>Editar categoría</c:otherwise>
        </c:choose>
    </h1>
    <a class="btn btn-secundario"
       href="${pageContext.request.contextPath}/categoria/listar">← Volver</a>
</div>

<div class="tarjeta" style="max-width:560px;">
    <c:if test="${not empty error}">
        <div class="alerta alerta-error" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <form class="formulario" method="post"
          action="${pageContext.request.contextPath}/categoria/guardar">
        <c:if test="${not empty categoria}">
            <input type="hidden" name="id" value="${categoria.id}">
        </c:if>

        <div class="campo">
            <label for="nombre">Nombre de la categoría</label>
            <input type="text" id="nombre" name="nombre" required maxlength="100"
                   minlength="2" title="Entre 2 y 100 caracteres"
                   placeholder="Ej.: Novela"
                   value="<c:out value='${not empty categoria ? categoria.nombre : nombre}'/>">
            <span class="ayuda">Debe ser único (no se permiten duplicados).</span>
        </div>

        <div class="acciones">
            <button class="btn" type="submit">Guardar</button>
            <a class="btn btn-secundario"
               href="${pageContext.request.contextPath}/categoria/listar">Cancelar</a>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>
