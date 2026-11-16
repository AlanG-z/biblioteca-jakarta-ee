<%-- ============================================================
     Página de error: muestra el mensaje enviado por el servlet
     (ReglaNegocioException o error interno).
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Error"/>
<%@ include file="/WEB-INF/vista/cabecera.jspf" %>

<div class="acceso">
    <div class="tarjeta">
        <h1>Ups, algo salió mal</h1>
        <div class="alerta alerta-error" role="alert">
            <c:out value="${empty mensaje ? 'Error inesperado. Intente nuevamente.' : mensaje}"/>
        </div>
        <a class="btn" href="${pageContext.request.contextPath}/catalogo">Volver al catálogo</a>
    </div>
</div>

<%@ include file="/WEB-INF/vista/pie.jspf" %>
