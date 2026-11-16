# Sistema de Biblioteca — Préstamos de libros

Aplicación web **Jakarta EE 10** en patrón **MVC** para gestionar el préstamo de
libros de una biblioteca. Los clientes consultan el catálogo, piden préstamos y
consultan sus multas; los administradores gestionan categorías, libros,
préstamos, devoluciones y multas, y revisan/editan los usuarios (activar o
desactivar cuentas). Las altas de clientes se hacen en `/registro` y las de
administradores directamente en la base de datos.

| Capa | Tecnología |
|------|------------|
| Vistas | JSP + JSTL (sin scriptlets, sin lógica de negocio) |
| Controlador | Un único servlet (`Manejador`) + 3 filtros |
| Lógica de negocio | EJB Stateless (Session Facade) |
| Persistencia | JPA 3.0 (EclipseLink) |
| Base de datos | MySQL 8 |
| Servidor | GlassFish 7 / 8 |
| Build | Maven (WAR) |

---

## 1. Requisitos

- **JDK 11 o superior** (el proyecto compila con `source/target = 11`; en esta
  máquina se usó JDK 21).
- **Apache Maven 3.8+** (NetBeans trae uno incluido en
  `/usr/lib/apache-netbeans/java/maven`).
- **Eclipse GlassFish 7.0 o 8.0** (probado con **GlassFish 8.0.0**).
- **MySQL 8** corriendo en `localhost:3306`.
- **Apache NetBeans 17+** (con el maven-j2ee-plugins para desplegar).
- Conector **mysql-connector-j** en el servidor: copiar
  `mysql-connector-j-8.4.0.jar` a `glassfish/domains/domain1/lib/`
  (en esta máquina ya está).

---

## 2. Estructura del proyecto

```
biblioteca/
├── pom.xml                                  # Dependencias y build (Java 11, WAR)
├── README.md
├── TODO.md                                  # Pendientes del proyecto
└── src/main/
    ├── java/com/mycompany/biblioteca/
    │   ├── Categoria.java                   # Entidad JPA
    │   ├── Libro.java                       # Entidad JPA
    │   ├── Usuario.java                     # Entidad JPA (contraseña BCrypt)
    │   ├── Prestamo.java                    # Entidad JPA
    │   ├── Multa.java                       # Entidad JPA
    │   ├── Rol.java                         # Enum: CLIENTE, ADMIN
    │   ├── EstadoPrestamo.java              # Enum: ACTIVO, DEVUELTO, ATRASADO
    │   ├── Configuracion.java               # Constantes del negocio
    │   ├── ReglaNegocioException.java       # Excepción de negocio (@ApplicationException)
    │   ├── controlador/
    │   │   └── Manejador.java               # Único servlet (todas las rutas)
    │   ├── filtro/
    │   │   ├── CodificacionFiltro.java      # UTF-8 en petición y respuesta
    │   │   ├── AutenticacionFiltro.java     # Exige sesión (/mis-prestamos, /prestamo/*)
    │   │   └── AdminFiltro.java             # Exige sesión + rol ADMIN
    │   └── sesiones/
    │       ├── AbstractFacade.java          # CRUD genérico
    │       ├── CategoriaFacade.java
    │       ├── LibroFacade.java
    │       ├── UsuarioFacade.java           # Login con BCrypt
    │       ├── PrestamoFacade.java          # Reglas de préstamo y multas
    │       └── MultaFacade.java
    ├── resources/META-INF/
    │   └── persistence.xml                  # Unidad my_persistence_unit → jdbc/biblioteca
    ├── setup/
    │   └── biblioteca.sql                   # Esquema + datos de ejemplo (idempotente)
    └── webapp/
        ├── index.jsp                        # Redirige al catálogo
        ├── css/style.css                    # Hoja de estilos activa (variables + mobile-first)
        ├── css/estilo.css                   # Diseño anterior, ya no referenciado (se conserva)
        ├── js/buscador.js                   # Búsqueda en vivo (solo en páginas con buscador)
        └── WEB-INF/
            ├── web.xml                      # Welcome file y timeout de sesión
            ├── beans.xml                    # CDI (bean-discovery-mode=all)
            ├── glassfish-web.xml            # Context-root /biblioteca
            └── vista/                       # Vistas (no accesibles por URL directa)
                ├── cabecera.jspf            # <head> + menú dinámico
                ├── pie.jspf                 # Cierra </main> e incluye buscador.js
                ├── paginacion.jspf          # Controles de paginación (10 por página)
                ├── buscador-libros.jspf     # Buscador reutilizable (texto + categoría)
                ├── error.jsp
                ├── login.jsp
                ├── registro.jsp
                ├── catalogo.jsp
                ├── libro-detalle.jsp
                ├── mis-prestamos.jsp
                ├── categoria-listar.jsp / categoria-form.jsp
                ├── libro-listar.jsp     / libro-form.jsp
                ├── usuario-listar.jsp   / usuario-form.jsp
                ├── prestamo-listar.jsp  / prestamo-form.jsp
                └── multa-listar.jsp
```

---

## 3. Base de datos

El script `src/main/setup/biblioteca.sql` **es idempotente**: crea la base
`biblioteca` si no existe, crea las 5 tablas, migra columnas faltantes
(`created_at`, `rol`/`estado` a ENUM en mayúsculas) y carga datos de ejemplo
**sin borrar ni duplicar lo que ya exista** (`INSERT IGNORE`).

```bash
mysql -uUSUARIO -p < src/main/setup/biblioteca.sql
```

Tablas: `categorias`, `libros`, `usuarios`, `prestamos`, `multas`.

> Los enumerados Java usan `EnumType.STRING`, por eso en MySQL los ENUM van en
> **mayúsculas** (`'CLIENTE'`, `'ACTIVO'`, …).

---

## 4. Pool JDBC en GlassFish (`asadmin`)

La aplicación pide un DataSource JNDI llamado **`jdbc/biblioteca`**. Si aún no
existe en tu GlassFish, créalo desde la consola (`asadmin` o la GUI en
`http://localhost:4848`):

```bash
# 1) Pool de conexiones hacia MySQL
asadmin create-jdbc-connection-pool \
  --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
  --restype javax.sql.ConnectionPoolDataSource \
  --property serverName=localhost:portNumber=3306:databaseName=biblioteca:\
user=USUARIO_MYSQL:password=CLAVE_MYSQL:useSSL=false:allowPublicKeyRetrieval=true \
  bibliotecaPool

# 2) Recurso JNDI que usa el persistence.xml
asadmin create-jdbc-resource --poolname bibliotecaPool jdbc/biblioteca

# 3) (Opcional) probar el pool
asadmin ping-connection-pool bibliotecaPool
```

Reemplaza `USUARIO_MYSQL` / `CLAVE_MYSQL` por los tuyos. En esta máquina el pool
ya está creado (`bibliotecaPool` → `jdbc/biblioteca` con el usuario `AlanDB`).

**Importante:** el driver debe estar en el servidor, no solo en el proyecto:
copia `mysql-connector-j-8.4.0.jar` a `glassfish/domains/domain1/lib/` y
reinicia GlassFish.

---

## 5. Cómo compilar y ejecutar en NetBeans

1. **Abrir el proyecto**: `Archivo → Abrir proyecto…` y seleccionar la carpeta
   `biblioteca`.
2. Asegurarse de que el servidor apuntado sea **GlassFish** (en esta máquina
   aparece como `gfv800ee11` en `nb-configuration.xml`).
3. **Clean and Build Project** (`F11`): genera
   `target/biblioteca-1.0-SNAPSHOT.war`.
4. **Run Project** (`F6`): NetBeans despliega en GlassFish y abre el navegador en
   `http://localhost:8080/biblioteca/`.

### Por línea de comandos

```bash
mvn clean package                                  # genera el WAR
cp target/biblioteca-1.0-SNAPSHOT.war \
   $GLASSFISH_HOME/glassfish/domains/domain1/autodeploy/
# GlassFish lo despliega automáticamente; o bien:
$GLASSFISH_HOME/bin/asadmin deploy --force=true target/biblioteca-1.0-SNAPSHOT.war
```

URL de la aplicación: **<http://localhost:8080/biblioteca/>**

---

## 6. Credenciales de prueba

| Rol | Email | Contraseña |
|-----|-------|------------|
| Administrador | `admin@biblioteca.com` | `admin123` |
| Cliente | `cliente@biblioteca.com` | `cliente123` |

Las contraseñas están hasheadas con **BCrypt** en la tabla `usuarios`. Cualquier
puede darse de alta desde **Registrarse** (`/registro`).

---

## 7. Rutas principales

| Método | Ruta | Quién | Descripción |
|--------|------|-------|-------------|
| GET/POST | `/login` | público | Iniciar sesión |
| GET | `/logout` | cualquiera | Cerrar sesión |
| GET/POST | `/registro` | público | Alta de cliente |
| GET | `/catalogo` | público | Catálogo con búsqueda y filtro |
| GET | `/libro/detalle?id=` | público | Detalle de un libro |
| GET | `/Categoria?codigoCat=` | público | Libros de una categoría (menú) |
| GET | `/mis-prestamos` | con sesión | Préstamos y multas del cliente |
| POST | `/prestamo/pedir` | con sesión | Pedir un libro (`libroId`) |
| GET | `/categoria/listar` … | ADMIN | Módulo categorías |
| GET | `/libro/listar` … | ADMIN | Módulo libros |
| GET | `/usuario/listar`, `/usuario/editar?id=` | ADMIN | Listar y editar usuarios (sin alta ni borrado) |
| POST | `/usuario/guardar` | ADMIN | Editar datos, rol y estado de un usuario |
| GET | `/prestamo/listar` … | ADMIN | Módulo préstamos |
| GET | `/multa/listar` … | ADMIN | Módulo multas |
| POST | `*/guardar`, `*/eliminar` | ADMIN | Guardar y eliminar **solo POST** (usuarios: solo guardar) |
| POST | `/prestamo/devolver` | ADMIN | Registrar devolución (genera multa) |

Las vistas viven en `WEB-INF/vista/`, por lo que **no se pueden abrir por URL
directa** (GlassFish responde 404). Después de cada alta, edición o baja se
**redirige al listado** (redirect-after-POST).

---

## 8. Reglas de negocio

Definidas en los facades (`PrestamoFacade`, `LibroFacade`, `UsuarioFacade`,
`CategoriaFacade`), nunca en el servlet ni en los JSP. Las constantes están en
`Configuracion`:

- **Préstamo de 7 días** (`DIAS_PRESTAMO`): `fecha_devolucion_prevista = hoy + 7`.
- **Stock**: pedir descuenta 1 ejemplar disponible; devolver lo devuelve.
- **Sin stock** ⇒ no se puede pedir (`ReglaNegocioException`).
- **Multas impagas** ⇒ el cliente no puede pedir libros hasta pagar.
- **Atraso** ⇒ multa = `días_atraso × MONTO_MULTA_DIARIO` (1.50/día). Se genera
  **automática al detectar el préstamo vencido** (al listar préstamos o
  multas), sin esperar a la devolución, y se ajusta al devolver. Nunca se cobra
  dos veces por los mismos días: solo la diferencia con lo ya pagado.
- **ATRASADO**: un préstamo no devuelto cuya fecha prevista pasó se marca
  ATRASADO automáticamente al listar.
- **No se puede eliminar** una categoría con libros ni un libro con préstamos.
- **Usuarios sin alta ni borrado en la aplicación**: los clientes se dan de alta
  en `/registro` y el administrador solo **lista y edita** (nombre, email, rol y
  cuenta activa/inactiva). Para dar de alta un administrador, insértelo en la
  tabla `usuarios` con la contraseña hasheada con BCrypt.
- **Validación en tres capas** de todos los formularios:
  1. **Cliente (HTML5)**: `required`, `minlength`, `maxlength`, `pattern`
     (ISBN), `min` (fecha de prórroga) y `title` con el mensaje a mostrar.
  2. **Servidor (`Manejador`)**: parámetros numéricos y de fecha leídos sin
     lanzar excepciones; si falta el `id` responde 400 y si no existe 404; los
     errores de negocio devuelven el formulario con el mensaje y los valores
     repoblados.
  3. **Negocio (`Configuracion` + facades)**: longitudes máximas
     (`NOMBRE_MAXIMO`, `EMAIL_MAXIMO`, `TITULO_MAXIMO`, `AUTOR_MAXIMO`,
     `ISBN_MAXIMO`), email e ISBN por regex y campos obligatorios ⇒
     `ReglaNegocioException` (evita el error 500 por columnas desbordadas).
- **Prórroga**: el administrador puede cambiar la fecha prevista de un préstamo
  (nunca anterior a la fecha del préstamo). Si deja el préstamo **sin atraso**,
  se **cancela la multa pendiente** (las ya pagadas se conservan); si sigue
  atrasado, se recalcula.
- Contraseñas con **mínimo 6 caracteres**, hasheadas con **BCrypt**; email con
  formato validado y único; ISBN opcional validado (10 o 13 dígitos).

---

## 9. Decisiones técnicas

- **Un solo servlet** (`Manejador`) con `switch` sobre `getServletPath()`; sin
  SQL ni JPQL en el controlador (solo facades EJB).
- **JSTL lo provee GlassFish** (`jakarta.tags.core` en `wasp.jar`): no se agregó
  dependencia JSTL al `pom.xml`.
- **Sin frameworks de estilos**: todo el CSS está en `css/style.css` con
  variables CSS, mobile-first (Grid/Flexbox + media queries) y **sin colores
  hardcodeados en los JSP**.
- **Fechas en formato ISO** (`yyyy-MM-dd`): `fmt:formatDate` de JSTL solo acepta
  `java.util.Date` y no `java.time.LocalDate`.
- **`eclipseLink.cache.shared.default=false`**: la base de datos es la única
  fuente de verdad, así los ejemplares disponibles nunca quedan desfasados.
- **Seguridad**: login real contra la tabla `usuarios` con BCrypt; el usuario va
  en `HttpSession`; `AutenticacionFiltro` protege rutas de cliente y `AdminFiltro`
  exige rol `ADMIN` en todas las rutas de gestión.
- **Errores de negocio** (`ReglaNegocioException`) se muestran en el formulario o
  en `error.jsp`; los `save`/`delete` solo aceptan POST y responden con redirect.
- **Portadas de libros**: el formulario de libro es `multipart/form-data`
  (`@MultipartConfig`, 5 MB). Si se sube un archivo, `guardarPortada()` valida la
  extensión (`.jpg/.jpeg/.png/.gif/.webp`), genera un nombre con timestamp +
  `UUID` (nunca el nombre del cliente) y lo guarda en `img/portadas/`; si no se
  sube nada, se conserva la URL escrita en el campo `portada`. Las vistas usan
  `<img>` con `onerror` para mostrar "Sin portada" si la URL no carga.
- **Las imágenes subidas viven en el directorio desplegado**
  (`applications/biblioteca/img/portadas/`): **se pierden al redeploy**. Mantenga
  una copia en `src/main/webapp/img/portadas/` (el `.gitkeep` de esa carpeta
  hace que Maven la incluya en el WAR).
- **Los préstamos solo los piden los clientes** (`/prestamo/pedir`): el
  administrador no da de alta préstamos, solo los lista, prorroga
  (`/prestamo/editar` + `/prestamo/guardar`, requiere `id`), devuelve y elimina.

---

## 10. Paginación

- Todos los listados (catálogo público, libros, categorías, usuarios, préstamos
  y multas) se reparten en **páginas de 10 registros** (`Configuracion.PAGINA_TAMANO`).
- Los controles viven en el fragmento **`WEB-INF/vista/paginacion.jspf`**, que
  se incluye **una sola vez por lista**, dentro del bloque de resultados.
- Se navega con botones «‹ Anterior · 1 2 … · Siguiente ›» y se muestra
  «Mostrando 1–10 de N». Si solo hay una página, el bloque no se dibuja.
- `Pagina` conserva los **parámetros de búsqueda** (`q`, `categoria`), así que
  al pasar de página no se pierde el filtro:
  `/libro/listar?pagina=2&categoria=1`.
- En «Mis préstamos» hay dos listas (préstamos y multas); ahí se hace
  `<c:set var="pagina" value="${paginaPrestamos|paginaMultas}"/>` antes de
  incluir el fragmento.

---

## 11. Búsqueda (con búsqueda en vivo)

### Formularios

| Dónde | Fragmento | Parámetros | Busca por |
|---|---|---|---|
| `/catalogo` (público) | `buscador-libros.jspf` | `q`, `categoria` | título o autor + categoría |
| `/libro/listar` (ADMIN) | `buscador-libros.jspf` (reutilizado) | `q`, `categoria` | título o autor + categoría |
| `/prestamo/listar` (ADMIN) | formulario propio | `q` | nombre o email de la persona, o título del libro |

- `buscador-libros.jspf` es **reutilizable**: la vista fija el destino con
  `<c:set var="accionBuscador" value="…"/>` antes de incluirlo; si no se define,
  usa `/catalogo`. Si no hay `q` ni `categoria`, cae al listado completo.
- La consulta vive en los facades: `LibroFacade.buscar/contarBuscar` y
  `PrestamoFacade.buscar/contarBuscar` (JPQL `LOWER(...) LIKE :t`, siempre
  paginados). Sin texto devuelven todo, igual que `pagina()`.
- Cada listado informa «N resultados · texto: «…»» y muestra un estado vacío
  distinto según haya filtro o no.

### Búsqueda en vivo (`js/buscador.js`)

- Al escribir en el input o cambiar la categoría, espera **300 ms** (debounce),
  pide la **misma URL** por `fetch` y reemplaza en la página solo los bloques
  marcados con `data-busqueda="resumen|contador|resultados"`.
- El formulario **nunca se toca**: el foco y el cursor quedan donde está el
  usuario; `history.replaceState` mantiene la URL al día.
- El contador se renderiza **siempre** (con `hidden` cuando no hay filtro) para
  que pueda aparecer y desaparecer en ambos sentidos.
- Usa `AbortController` para cancelar el pedido anterior; mientras llega la
  respuesta, los bloques se atenúan (`.cargando`).
- Si el navegador no tiene `fetch`, el script no hace nada y el formulario
  sigue funcionando con el envío normal.
- Solo actúa donde hay `form.buscador`; en el resto de páginas no hace nada.

---

## 12. Pendientes

Ver [TODO.md](TODO.md).
