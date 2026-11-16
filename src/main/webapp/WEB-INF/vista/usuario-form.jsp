<%-- ============================================================
     Formulario de usuario (solo edición: alta en /registro).
     Atributos: usuario, error.
     La contraseña vacía conserva la anterior.
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Editar usuario"/>
<%@ include file="/WEB-INF/vista/cabecera.jspf" %>

<div class="pagina-cabecera">
    <h1>Editar usuario</h1>
    <a class="btn btn-secundario"
       href="${pageContext.request.contextPath}/usuario/listar">← Volver</a>
</div>

<div class="tarjeta" style="max-width:640px;">
    <c:if test="${not empty error}">
        <div class="alerta alerta-error" role="alert">
            <c:out value="${error}"/>
        </div>
    </c:if>

    <form class="formulario dos-columnas" method="post"
          action="${pageContext.request.contextPath}/usuario/guardar">
        <c:if test="${not empty usuario.id}">
            <input type="hidden" name="id" value="${usuario.id}">
        </c:if>

        <div class="campo campo-ancho">
            <label for="nombre">Nombre completo</label>
            <input type="text" id="nombre" name="nombre" required maxlength="100"
                   minlength="2" title="Entre 2 y 100 caracteres"
                   placeholder="Ej.: Juan Pérez"
                   value="<c:out value='${usuario.nombre}'/>">
        </div>

        <div class="campo campo-ancho">
            <label for="email">Email</label>
            <input type="email" id="email" name="email" required maxlength="150"
                   title="Ej.: nombre@dominio.com"
                   placeholder="ejemplo@biblioteca.com"
                   value="<c:out value='${usuario.email}'/>">
        </div>

        <div class="campo">
            <label for="password">Contraseña</label>
            <input type="password" id="password" name="password"
                   minlength="6" maxlength="64" autocomplete="new-password"
                   title="Mínimo 6 caracteres (déjela vacía para no cambiarla)"
                   placeholder="Dejar vacío para mantener">
            <span class="ayuda">Si la deja vacía, no se modifica.</span>
        </div>

        <div class="campo">
            <label for="rol">Rol</label>
            <select id="rol" name="rol" required>
                <option value="CLIENTE"
                        <c:if test="${usuario.rol == 'CLIENTE'}">selected</c:if>>Cliente</option>
                <option value="ADMIN"
                        <c:if test="${usuario.rol == 'ADMIN'}">selected</c:if>>Administrador</option>
            </select>
        </div>

        <div class="campo campo-ancho">
            <label class="campo-check">
                <input type="checkbox" name="activo" value="on"
                       <c:if test="${usuario.activo}">checked</c:if>>
                Cuenta activa (puede iniciar sesión y pedir libros)
            </label>
        </div>

        <div class="acciones campo-ancho">
            <button class="btn" type="submit">Guardar</button>
            <a class="btn btn-secundario"
               href="${pageContext.request.contextPath}/usuario/listar">Cancelar</a>
        </div>
    </form>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>
