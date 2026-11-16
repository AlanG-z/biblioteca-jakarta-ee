# TODO — Sistema de Biblioteca

Pendientes y mejoras propuestas, ordenados por prioridad.

## Prioridad alta

- [x] **Portadas de libros**: las vistas (`catalogo.jsp`, `libro-detalle.jsp` y
      `libro-listar.jsp`) muestran la imagen con placeholder "Sin portada" y
      `onerror` para cuando la URL no carga.
- [x] **Subida de imágenes**: `libro-form.jsp` usa `multipart/form-data` con el
      campo `archivoPortada`; `Manejador.guardarPortada()` valida la extensión
      (JPG/PNG/GIF/WEBP, 5 MB), genera el nombre en el servidor y guarda en
      `img/portadas/`. Si no se sube nada, se usa la URL del campo `portada`.
- [ ] **Tests unitarios (JUnit)** de las reglas de negocio de
      `PrestamoFacade` (stock, multas, atrasos, bloqueo por multas) y de
      `UsuarioFacade` (BCrypt, email duplicado).
- [ ] **Perfil del cliente**: cambiar contraseña y ver/editar sus datos desde
      `/mis-prestamos`.

## Prioridad media

- [x] **Paginación** de todos los listados y del catálogo: 10 por página,
      fragmento `paginacion.jspf`, conserva los filtros de búsqueda.
- [x] **Búsqueda** en el catálogo y en los listados de libros (título o autor +
      categoría) y de préstamos (persona, email o título del libro), con
      **búsqueda en vivo** (`js/buscador.js`).
- [ ] **Búsqueda por ISBN** con coincidencia parcial (hoy es título/autor).
- [ ] **Reportes del administrador**: préstamos por período, libros más
      prestados, multas cobradas/pendientes.
- [ ] **Historial de multas pagadas**: `/multa/listar` solo muestra las
      pendientes; falta un listado con las pagadas y su fecha de pago.
- [ ] **Recuperar contraseña** por email (hoy solo un admin puede resetearla
      desde `/usuario/editar`).
- [ ] **Auditoría**: guardar quién registró/devolvió cada préstamo
      (hoy solo existe `created_at` en `libros` y `usuarios`).

## Prioridad baja

- [ ] **Aviso de vencimiento**: usar un `@Schedule` (TimerService) para marcar
      ATRASADO y/o enviar un email de recordatorio un día antes del vencimiento.
- [ ] **Internacionalización** de los textos con `fmt:message` (es/en).
- [ ] **Pagos parciales de multas** y comprobante de pago.
- [ ] **Exportar** el listado de préstamos/multas a CSV o PDF.
- [ ] **Docker Compose** con MySQL + GlassFish para levantar todo en un comando.
- [ ] **Préstamos reservados**: cola de espera cuando un libro está agotado.
- [ ] **Regla opcional de préstamo**: impedir que un usuario tenga el mismo libro
      prestado más de una vez al mismo tiempo (hoy solo se controla el stock
      global y las multas impagas).
- [x] **Dark mode** automático con `prefers-color-scheme` en `css/style.css`
      (sin `localStorage`).

## Pendientes del entorno

- [ ] El proyecto apunta a **Jakarta EE 10 / Java 11** y corre en el GlassFish
      **8.0.0** instalado; si se requiere GlassFish 7, solo hay que cambiar el
      servidor en NetBeans (el código es compatible con ambos).
- [ ] `asadmin` en esta máquina pide autenticación; el despliegue de prueba se
      hizo copiando el WAR a `glassfish/domains/domain1/autodeploy/`.
