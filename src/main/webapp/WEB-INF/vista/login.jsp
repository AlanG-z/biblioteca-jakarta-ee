<%-- ============================================================
     Formulario de inicio de sesión.
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/vista/cabecera.jspf" %>

<div class="acceso">
    <div class="tarjeta" style="max-width: 380px;">
        <h1>Iniciar sesión</h1>

        <c:if test="${not empty exito}">
            <div class="alerta alerta-exito" role="status">
                <c:out value="${exito}"/>
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="alerta alerta-error" role="alert">
                <c:out value="${error}"/>
            </div>
        </c:if>

        <form class="formulario" method="post"
              action="${pageContext.request.contextPath}/login">
            <div class="campo">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" required
                       class="input"
                       autocomplete="username"
                       placeholder="ejemplo@biblioteca.com">
            </div>
            <div class="campo">
                <label for="password">Contraseña</label>
                <input type="password" id="password" name="password" required
                       class="input"
                       minlength="6"
                       placeholder="••••••">
                <span class="ayuda">Mínimo 6 caracteres.</span>
            </div>

            <div class="recordatorio">
                <label>
                    <input type="checkbox" id="recordar" name="recordar">
                    Recordarme
                </label>
            </div>

            <div class="form-actions">
                <button class="btn btn-primario" type="submit">Entrar</button>
            </div>

            <p class="pie-acceso">
                ¿No tiene cuenta?
                <a href="${pageContext.request.contextPath}/registro">Regístrate</a>
            </p>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>