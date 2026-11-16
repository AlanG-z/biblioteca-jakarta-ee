<%-- ============================================================
     Formulario de libro (alta y edición).
     Atributos: libro (opcional, para editar), categorias, error.
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="${empty libro.id ? 'Nuevo libro' : 'Editar libro'}"/>
<%@ include file="/WEB-INF/vista/cabecera.jspf" %>

<div class="pagina-cabecera">
    <h1>
        <c:choose>
            <c:when test="${empty libro.id}">Nuevo libro</c:when>
            <c:otherwise>Editar libro</c:otherwise>
        </c:choose>
    </h1>
    <a class="btn btn-secundario"
       href="${pageContext.request.contextPath}/libro/listar">← Volver</a>
</div>

<div class="tarjeta" style="max-width:720px;">
    <c:if test="${not empty error}">
        <div class="alerta alerta-error" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>

  <form class="formulario dos-columnas" method="post"
      enctype="multipart/form-data"
      action="${pageContext.request.contextPath}/libro/guardar">
        <c:if test="${not empty libro.id}">
            <input type="hidden" name="id" value="${libro.id}">
        </c:if>

        <div class="campo campo-ancho">
            <label for="titulo">Título</label>
            <input type="text" id="titulo" name="titulo" required maxlength="200"
                   minlength="2" title="Entre 2 y 200 caracteres"
                   placeholder="Ej.: Cien años de soledad"
                   value="<c:out value='${libro.titulo}'/>">
        </div>

        <div class="campo">
            <label for="autor">Autor</label>
            <input type="text" id="autor" name="autor" required maxlength="150"
                   minlength="2" title="Entre 2 y 150 caracteres"
                   placeholder="Ej.: Gabriel García Márquez"
                   value="<c:out value='${libro.autor}'/>">
        </div>

        <div class="campo">
            <label for="isbn">ISBN (opcional)</label>
            <input type="text" id="isbn" name="isbn" maxlength="13"
                   pattern="\d{9}[\dXx]|\d{13}"
                   title="ISBN de 10 o 13 dígitos (déjelo vacío si no tiene)"
                   placeholder="10 o 13 dígitos"
                   value="<c:out value='${libro.isbn}'/>">
        </div>

        <div class="campo">
            <label for="categoriaId">Categoría</label>
            <select id="categoriaId" name="categoriaId" required>
                <option value="">— Seleccione —</option>
                <c:forEach var="cat" items="${categorias}">
                    <option value="${cat.id}"
                            <c:if test="${libro.categoria.id == cat.id}">selected</c:if>>
                        <c:out value="${cat.nombre}"/>
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="campo">
            <label for="ejemplaresTotal">Ejemplares totales</label>
            <input type="number" id="ejemplaresTotal" name="ejemplaresTotal"
                   required min="1" step="1"
                   value="${empty libro.ejemplaresTotal ? 1 : libro.ejemplaresTotal}">
            <span class="ayuda">Los disponibles arrancan iguales a los totales.</span>
        </div>

<div class="campo campo-ancho">
    <%---
    <label for="archivoPortada">Subir imagen de portada</label>
    <input type="file" id="archivoPortada" name="archivoPortada" accept="image/*">
    <span class="ayuda">JPG, PNG, GIF o WEBP, máximo 5 MB.</span> --%>
    <c:if test="${not empty libro.portada}">
        <img class="portada-preview"
             src="<c:out value='${libro.portada}'/>"
             alt="Portada actual" onerror="this.remove()">
        <span class="ayuda">Imagen actual (si sube otra, la reemplaza).</span>
    </c:if>
</div>

<div class="campo campo-ancho">
    <label for="portada">Pegue una URL (opcional)</label>
    <input type="url" id="portada" name="portada" maxlength="255"
           placeholder="https://…"
           value="<c:out value='${libro.portada}'/>">
</div>

        <div class="acciones campo-ancho">
            <button class="btn" type="submit">Guardar</button>
            <a class="btn btn-secundario"
               href="${pageContext.request.contextPath}/libro/listar">Cancelar</a>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>
