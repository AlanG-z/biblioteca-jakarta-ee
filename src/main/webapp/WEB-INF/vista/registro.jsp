<%-- ============================================================
     Formulario de registro de clientes.
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/vista/cabecera.jspf" %>

<div class="acceso">
    <div class="tarjeta" style="max-width: 420px;">
        <h1>Crear cuenta</h1>
        <p class="muted chico">Complete sus datos para registrarse como cliente.</p>

        <c:if test="${not empty error}">
            <div class="alerta alerta-error" role="alert">
                <c:out value="${error}"/>
            </div>
        </c:if>

        <form class="formulario" method="post"
              action="${pageContext.request.contextPath}/registro">
            <div class="campo">
                <label for="nombre">Nombre completo</label>
                <input type="text" id="nombre" name="nombre" required
                       class="input"
                       maxlength="100"
                       value="<c:out value='${nombre}'/>"
                       placeholder="Juan Pérez">
            </div>
            <div class="campo">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" required
                       class="input"
                       maxlength="150"
                       autocomplete="username"
                       value="<c:out value='${email}'/>"
                       placeholder="ejemplo@biblioteca.com">
            </div>
            <div class="campo">
                <label for="password">Contraseña</label>
                <input type="password" id="password" name="password" required
                       class="input"
                       minlength="6"
                       maxlength="64"
                       autocomplete="new-password">
                <span class="ayuda">Mínimo 6 caracteres.</span>
            </div>
            <div class="campo">
                <label for="repetir">Repetir contraseña</label>
                <input type="password" id="repetir" name="repetir" required
                       class="input"
                       minlength="6"
                       maxlength="64"
                       autocomplete="new-password"
                       placeholder="Repita la contraseña">
            </div>
            <div class="form-actions">
                <button class="btn btn-primario" type="submit">Registrarse</button>
                <a class="btn btn-secundario" href="${pageContext.request.contextPath}/login">Ya tengo cuenta</a>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>