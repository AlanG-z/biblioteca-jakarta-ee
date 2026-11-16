package com.mycompany.biblioteca.controlador;

import com.mycompany.biblioteca.Categoria;
import com.mycompany.biblioteca.EstadoPrestamo;
import com.mycompany.biblioteca.Libro;
import com.mycompany.biblioteca.Multa;
import com.mycompany.biblioteca.Pagina;
import com.mycompany.biblioteca.Prestamo;
import com.mycompany.biblioteca.ReglaNegocioException;
import com.mycompany.biblioteca.Rol;
import com.mycompany.biblioteca.Usuario;
import com.mycompany.biblioteca.sesiones.CategoriaFacade;
import com.mycompany.biblioteca.sesiones.LibroFacade;
import com.mycompany.biblioteca.sesiones.MultaFacade;
import com.mycompany.biblioteca.sesiones.PrestamoFacade;
import com.mycompany.biblioteca.sesiones.UsuarioFacade;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.IntFunction;

import jakarta.servlet.annotation.MultipartConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
/**
 * Único servlet controlador de la aplicación: recibe todas las rutas,
 * llama a los facades EJB y reenvía a las JSP de WEB-INF/vista.
 * No contiene SQL ni JPQL: solo lógica de control y validación de entrada.
 *
 * Convención: después de cada alta, edición o baja se redirige al
 * listado (redirect-after-POST). Guardar y eliminar solo aceptan POST.
 */
@WebServlet(name = "Manejador", urlPatterns = {
    // Públicas
    "/login", "/logout", "/registro", "/catalogo", "/libro/detalle",
    // Cliente (requieren sesión: las protege AutenticacionFiltro)
    "/mis-prestamos", "/prestamo/pedir", "/prestamo/pagar-multa",
    // Administrador (las protege AdminFiltro)
    "/categoria/listar", "/categoria/nuevo", "/categoria/editar",
    "/categoria/guardar", "/categoria/eliminar",
    "/libro/listar", "/libro/nuevo", "/libro/editar",
    "/libro/guardar", "/libro/eliminar",
    // Usuarios: solo listado y edición (el alta es /registro y los
    // administradores se crean directamente en la base de datos)
    "/usuario/listar", "/usuario/editar", "/usuario/guardar",
    "/prestamo/listar",  "/prestamo/editar",
    "/prestamo/guardar", "/prestamo/eliminar", "/prestamo/devolver",
    "/multa/listar", "/multa/guardar", "/multa/eliminar",
    "/Categoria"
})
@MultipartConfig(
        maxFileSize=5*1024*1024,
        maxRequestSize=6*1024*1024
       
        )
public class Manejador extends HttpServlet {

    /** Todas las vistas viven dentro de WEB-INF/vista (no accesibles por URL). */
    private static final String VISTA = "/WEB-INF/vista/";

    @EJB private CategoriaFacade categoriaFacade;
    @EJB private LibroFacade libroFacade;
    @EJB private UsuarioFacade usuarioFacade;
    @EJB private PrestamoFacade prestamoFacade;
    @EJB private MultaFacade multaFacade;

    // ============================================================
    //  Enrutamiento
    // ============================================================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        procesar(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        procesar(req, resp);
    }

    private void procesar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String ruta = req.getServletPath();
        String url;

        try {
            switch (ruta) {
                // ---------- Públicas ----------
                case "/login":         url = login(req, resp);           break;
                case "/logout":        url = logout(req, resp);          break;
                case "/registro":      url = registro(req, resp);        break;
                case "/catalogo":      url = catalogo(req, resp);        break;
                case "/libro/detalle": url = detalleLibro(req, resp);    break;

                // ---------- Cliente ----------
                case "/mis-prestamos":  url = misPrestamos(req, resp);   break;
                case "/prestamo/pedir": url = pedirPrestamo(req, resp);  break;

                // ---------- Categorías ----------
                case "/categoria/listar":  url = listarCategorias(req);          break;
                case "/categoria/nuevo":   url = formCategoria(req, null);       break;
                case "/categoria/editar":  url = formCategoria(req, resp);       break;
                case "/categoria/guardar": url = guardarCategoria(req, resp);    break;
                case "/categoria/eliminar":url = eliminarCategoria(req, resp);   break;

                // ---------- Libros ----------
                case "/libro/listar":  url = listarLibros(req);          break;
                case "/libro/nuevo":   url = formLibro(req, null);       break;
                case "/libro/editar":  url = formLibro(req, resp);       break;
                case "/libro/guardar": url = guardarLibro(req, resp);    break;
                case "/libro/eliminar":url = eliminarLibro(req, resp);   break;

                // ---------- Usuarios (solo listado y edición) ----------
                case "/usuario/listar":  url = listarUsuarios(req);      break;
                case "/usuario/editar":  url = formUsuario(req, resp);   break;
                case "/usuario/guardar": url = guardarUsuario(req, resp);break;

                // ---------- Préstamos ----------
                case "/prestamo/listar":  url = listarPrestamos(req);    break;
             
                case "/prestamo/editar":  url = formPrestamo(req, resp); break;
                case "/prestamo/guardar": url = guardarPrestamo(req, resp); break;
                case "/prestamo/eliminar":url = eliminarPrestamo(req, resp); break;
                case "/prestamo/devolver":url = devolverPrestamo(req, resp); break;

                // ---------- Multas ----------
                case "/multa/listar":   url = listarMultas(req);         break;
                case "/multa/guardar":  url = guardarMulta(req, resp);   break;
                case "/multa/eliminar": url = eliminarMulta(req, resp);  break;
                case "/prestamo/pagar-multa": url = pagarMultaCliente(req, resp); break;

                // ---------- Libros por categoría (desde el menú) ----------
                case "/Categoria": url = catalogoCategoria(req, resp);  break;

                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            req.setAttribute("mensaje", "Parámetro inválido: revise los datos enviados.");
            url = VISTA + "error.jsp";
        } catch (Exception e) {
            // Error inesperado: se muestra en la vista de error
            e.printStackTrace();
            req.setAttribute("mensaje", "Error interno: " + e.getMessage());
            url = VISTA + "error.jsp";
        }

        if (url == null) {
            return; // la operación ya respondió con un redirect
        }
        consumirExito(req);
        req.getRequestDispatcher(url).forward(req, resp);
    }

    /**
     * Redirige dejando un mensaje de éxito "flash": se guarda en la sesión y
     * la vista que se muestre después del redirect lo copia a la request y lo
     * borra, de modo que el mensaje aparece una sola vez (redirect-after-POST).
     *
     * @param ruta    ruta absoluta dentro de la aplicación (ej. "/libro/listar")
     * @param mensaje texto que verá el usuario en la alerta de éxito
     */
    private String redirigirConExito(HttpServletRequest req,
            HttpServletResponse resp, String ruta, String mensaje)
            throws IOException {
        HttpSession sesion = req.getSession(true);
        sesion.setAttribute("exito", mensaje);
        resp.sendRedirect(req.getContextPath() + ruta);
        return null;
    }

    /** Pasa a la request el mensaje flash pendiente y lo consume. */
    private void consumirExito(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        if (sesion == null) {
            return;
        }
        Object exito = sesion.getAttribute("exito");
        if (exito != null) {
            sesion.removeAttribute("exito");
            req.setAttribute("exito", exito);
        }
    }

    // ============================================================
    //  Públicas: login, logout, registro, catálogo
    // ============================================================

    /** Muestra el formulario (GET) o valida las credenciales (POST). */
    private String login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (esPost(req)) {
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            Usuario usuario = usuarioFacade.autenticar(email, password);

            if (usuario == null) {
                req.setAttribute("error", "Email o contraseña incorrectos, o la cuenta está desactivada.");
                req.setAttribute("email", email == null ? "" : email);
                return VISTA + "login.jsp";
            }

            // Se guarda el usuario en la sesión
            HttpSession sesion = req.getSession(true);
            sesion.setAttribute("usuario", usuario);
            sesion.setAttribute("esAdmin", usuario.getRol() == Rol.ADMIN);

            if (usuario.getRol() == Rol.ADMIN) {
                resp.sendRedirect(req.getContextPath() + "/libro/listar");
            } else {
                resp.sendRedirect(req.getContextPath() + "/catalogo");
            }
            return null;
        }
        return VISTA + "login.jsp";
    }

    /** Cierra la sesión y vuelve al login. */
    private String logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        // La sesión ya no existe: se crea una nueva solo para el mensaje flash
        return redirigirConExito(req, resp, "/login",
                "Sesión cerrada correctamente.");
    }

    /** Alta de cliente (GET formulario / POST creación). */
    private String registro(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (esPost(req)) {
            String nombre = req.getParameter("nombre");
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            String repetir = req.getParameter("repetir");

            try {
                if (password == null || !password.equals(repetir)) {
                    throw new ReglaNegocioException("Las contraseñas no coinciden.");
                }
                usuarioFacade.crear(nombre, email, password, Rol.CLIENTE, true);
            } catch (ReglaNegocioException e) {
                req.setAttribute("error", e.getMessage());
                req.setAttribute("nombre", nombre == null ? "" : nombre);
                req.setAttribute("email", email == null ? "" : email);
                return VISTA + "registro.jsp";
            }

            // Alta exitosa: se manda al login con mensaje de éxito
            return redirigirConExito(req, resp, "/login?registro=ok",
                    "Cuenta creada correctamente. Ya puede iniciar sesión con su email y contraseña.");
        }
        return VISTA + "registro.jsp";
    }

    /** Catálogo público: búsqueda por título/autor y filtro por categoría. */
    private String catalogo(HttpServletRequest req, HttpServletResponse resp) {
        String texto = req.getParameter("q");
        Integer idCategoria = parametroEntero(req, "categoria");
        prepararCatalogo(req, texto, idCategoria, "categoria");
        return VISTA + "catalogo.jsp";
    }

    /** Ruta /Categoria?codigoCat=ID: libros de una categoría (desde el menú). */
    private String catalogoCategoria(HttpServletRequest req, HttpServletResponse resp) {
        Integer idCategoria = parametroEntero(req, "codigoCat");
        if (idCategoria == null) {
            return catalogo(req, resp);
        }
        prepararCatalogo(req, null, idCategoria, "codigoCat");
        return VISTA + "catalogo.jsp";
    }

    /**
     * Carga los datos del catálogo en la request, paginando la búsqueda.
     * El combo de categorías se carga completo (es un desplegable, no una tabla).
     *
     * @param parametroCategoria nombre del parámetro que hay que conservar en
     *                           los enlaces de paginación ("categoria" o "codigoCat")
     */
    private void prepararCatalogo(HttpServletRequest req, String texto,
            Integer idCategoria, String parametroCategoria) {
        long total = libroFacade.contarBuscar(texto, idCategoria);
        Pagina<Libro> pagina = paginar(req, total,
                n -> libroFacade.buscar(texto, idCategoria, n),
                parametrosPagina(parametroCategoria,
                        idCategoria == null ? null : idCategoria.toString(),
                        "q", texto));
        req.setAttribute("libros", pagina.getElementos());
        req.setAttribute("pagina", pagina);
        req.setAttribute("categorias", categoriaFacade.findAll());
        req.setAttribute("q", texto == null ? "" : texto);
        req.setAttribute("categoriaSel", idCategoria);
        if (idCategoria != null) {
            req.setAttribute("categoriaActual", categoriaFacade.find(idCategoria));
        }
    }

    /** Detalle de un libro (público): /libro/detalle?id=X */
    private String detalleLibro(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Integer id = parametroEntero(req, "id");
        Libro libro = id == null ? null : libroFacade.find(id);
        if (libro == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Libro no encontrado");
            return null;
        }
        req.setAttribute("libro", libro);
        req.setAttribute("categorias", categoriaFacade.findAll());
        return VISTA + "libro-detalle.jsp";
    }

    // ============================================================
    //  Cliente: mis préstamos y pedir préstamo
    // ============================================================

    /** Lista los préstamos del usuario en sesión (y sus multas), paginadas. */
    private String misPrestamos(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Usuario usuario = usuarioEnSesion(req);
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        Integer idUsuario = usuario.getId();

        Pagina<Prestamo> pagPrestamos = paginar(req,
                prestamoFacade.contarPorUsuario(idUsuario),
                n -> prestamoFacade.porUsuario(idUsuario, n));
        Pagina<Multa> pagMultas = paginar(req,
                multaFacade.contarPorUsuario(idUsuario),
                n -> multaFacade.porUsuario(idUsuario, n));

        req.setAttribute("prestamos", pagPrestamos.getElementos());
        req.setAttribute("multas", pagMultas.getElementos());
        req.setAttribute("paginaPrestamos", pagPrestamos);
        req.setAttribute("paginaMultas", pagMultas);
        req.setAttribute("prestamosNoDevueltos",
                prestamoFacade.contarNoDevueltosPorUsuario(idUsuario));
        return VISTA + "mis-prestamos.jsp";
    }

    /** POST /prestamo/pedir: registra un préstamo del libro indicado. */
    private String pedirPrestamo(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        Usuario usuario = usuarioEnSesion(req);
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        Integer idLibro = parametroEntero(req, "libroId");
        try {
            prestamoFacade.pedir(usuario.getId(), idLibro);
        } catch (ReglaNegocioException e) {
            // Se vuelve al detalle del libro con el mensaje de error
            req.setAttribute("libro", idLibro == null ? null : libroFacade.find(idLibro));
            req.setAttribute("categorias", categoriaFacade.findAll());
            req.setAttribute("error", e.getMessage());
            return VISTA + "libro-detalle.jsp";
        }
        // Redirige después del POST
        return redirigirConExito(req, resp, "/mis-prestamos",
                "Préstamo solicitado correctamente. Ya figura en sus préstamos.");
    }

    // ============================================================
    //  Admin: Categorías
    // ============================================================

    private String listarCategorias(HttpServletRequest req) {
        Pagina<Categoria> pagina = paginar(req, categoriaFacade.contar(),
                categoriaFacade::pagina);
        req.setAttribute("categorias", pagina.getElementos());
        req.setAttribute("pagina", pagina);
        return VISTA + "categoria-listar.jsp";
    }

    /** Muestra el formulario. Con resp != null es edición (lee ?id=). */
    private String formCategoria(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (resp != null) {
            Integer id = parametroEntero(req, "id");
            Categoria categoria = id == null ? null : categoriaFacade.find(id);
            if (categoria == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Categoría no encontrada");
                return null;
            }
            req.setAttribute("categoria", categoria);
        }
        return VISTA + "categoria-form.jsp";
    }

    private String guardarCategoria(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        String id = req.getParameter("id");
        String nombre = req.getParameter("nombre");
        try {
            if (id == null || id.isBlank()) {
                categoriaFacade.crear(nombre);
            } else {
                categoriaFacade.actualizar(Integer.valueOf(id), nombre);
            }
        } catch (ReglaNegocioException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("categorias", categoriaFacade.findAll());
            req.setAttribute("nombre", nombre == null ? "" : nombre);
            return VISTA + "categoria-form.jsp";
        }
        boolean esAlta = id == null || id.isBlank();
        return redirigirConExito(req, resp, "/categoria/listar",
                esAlta ? "Categoría creada correctamente."
                       : "Categoría actualizada correctamente.");
    }

    private String eliminarCategoria(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        Integer id = parametroEntero(req, "id");
        try {
            categoriaFacade.eliminar(id);
        } catch (ReglaNegocioException e) {
            req.setAttribute("error", e.getMessage());
            Pagina<Categoria> pagina = paginar(req, categoriaFacade.contar(),
                    categoriaFacade::pagina);
            req.setAttribute("categorias", pagina.getElementos());
            req.setAttribute("pagina", pagina);
            return VISTA + "categoria-listar.jsp";
        }
        return redirigirConExito(req, resp, "/categoria/listar",
                "Categoría eliminada correctamente.");
    }

    // ============================================================
    //  Admin: Libros
    // ============================================================

    private String listarLibros(HttpServletRequest req) {
        // Búsqueda por título/autor (q) y filtro por categoría, reutilizando
        // las mismas consultas del catálogo público (LibroFacade.buscar).
        String texto = req.getParameter("q");
        Integer idCategoria = parametroEntero(req, "categoria");
        long total = libroFacade.contarBuscar(texto, idCategoria);
        Pagina<Libro> pagina = paginar(req, total,
                n -> libroFacade.buscar(texto, idCategoria, n),
                parametrosPagina("categoria",
                        idCategoria == null ? null : idCategoria.toString(),
                        "q", texto));
        req.setAttribute("libros", pagina.getElementos());
        req.setAttribute("pagina", pagina);
        req.setAttribute("ejemplaresDisponibles", libroFacade.sumaEjemplaresDisponibles());
        req.setAttribute("categorias", categoriaFacade.findAll());
        req.setAttribute("q", texto == null ? "" : texto.trim());
        req.setAttribute("categoriaSel", idCategoria);
        return VISTA + "libro-listar.jsp";
    }

    private String formLibro(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (resp != null) {
            Integer id = parametroEntero(req, "id");
            Libro libro = id == null ? null : libroFacade.find(id);
            if (libro == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Libro no encontrado");
                return null;
            }
            req.setAttribute("libro", libro);
        }
        req.setAttribute("categorias", categoriaFacade.findAll());
        return VISTA + "libro-form.jsp";
    }

    private String guardarLibro(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        String idParam = req.getParameter("id");
        Integer idLibro = parametroEntero(req, "id");
        if (idParam != null && !idParam.isBlank() && idLibro == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Identificador de libro inválido.");
            return null;
        }
        Integer idCategoria = parametroEntero(req, "categoriaId");
        String titulo = req.getParameter("titulo");
        String autor = req.getParameter("autor");
        String isbn = req.getParameter("isbn");
        String portada = req.getParameter("portada");

        // Validación de capa A/HTML5: "1" o "1.0" o "abc" no sirven aquí
        Integer ejemplares = parametroEntero(req, "ejemplaresTotal");
        if (ejemplares == null || ejemplares < 1) {
            req.setAttribute("error", "Los ejemplares totales deben ser un número entero mayor o igual a 1.");
            req.setAttribute("categorias", categoriaFacade.findAll());
            devolverValoresLibro(req, idCategoria, titulo, autor, isbn, portada, 1);
            return VISTA + "libro-form.jsp";
        }

        try {
              portada = guardarPortada(req, portada);     // ← NUEVO: el archivo pisa la URL

            if (idLibro == null) {
                libroFacade.crear(idCategoria, titulo, autor, isbn, portada, ejemplares);
            } else {
                libroFacade.actualizar(idLibro, idCategoria, titulo, autor,
                        isbn, portada, ejemplares);
            }
          
        } catch (ReglaNegocioException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("categorias", categoriaFacade.findAll());
            devolverValoresLibro(req, idCategoria, titulo, autor, isbn, portada, ejemplares);
            return VISTA + "libro-form.jsp";
        }
        boolean esAltaLibro = idLibro == null;
        return redirigirConExito(req, resp, "/libro/listar",
                esAltaLibro ? "Libro agregado al catálogo correctamente."
                            : "Libro actualizado correctamente.");
    }

    /** Repuebla el formulario de libro cuando hay un error de validación. */
    private void devolverValoresLibro(HttpServletRequest req, Integer idCategoria,
            String titulo, String autor, String isbn, String portada, int ejemplares) {
        Libro temporal = new Libro();
        temporal.setCategoria(idCategoria == null ? null : new Categoria(idCategoria));
        temporal.setTitulo(titulo);
        temporal.setAutor(autor);
        temporal.setIsbn(isbn);
        temporal.setPortada(portada);
        temporal.setEjemplaresTotal(ejemplares);
        req.setAttribute("libro", temporal);
    }

    private String eliminarLibro(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        Integer id = parametroEntero(req, "id");
        try {
            libroFacade.eliminar(id);
        } catch (ReglaNegocioException e) {
            req.setAttribute("error", e.getMessage());
            Pagina<Libro> pagina = paginar(req, libroFacade.contar(), libroFacade::pagina);
            req.setAttribute("libros", pagina.getElementos());
            req.setAttribute("pagina", pagina);
            req.setAttribute("ejemplaresDisponibles", libroFacade.sumaEjemplaresDisponibles());
            return VISTA + "libro-listar.jsp";
        }
        return redirigirConExito(req, resp, "/libro/listar",
                "Libro eliminado correctamente.");
    }

    // ============================================================
    //  Admin: Usuarios
    // ============================================================

    private String listarUsuarios(HttpServletRequest req) {
        Pagina<Usuario> pagina = paginar(req, usuarioFacade.contar(), usuarioFacade::pagina);
        req.setAttribute("usuarios", pagina.getElementos());
        req.setAttribute("pagina", pagina);
        req.setAttribute("administradores", usuarioFacade.contarAdministradores());
        return VISTA + "usuario-listar.jsp";
    }

    /**
     * Formulario de edición de un usuario (siempre requiere ?id=;
     * sin id o con un id inexistente responde 404).
     */
    private String formUsuario(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Integer id = parametroEntero(req, "id");
        Usuario usuario = id == null ? null : usuarioFacade.find(id);
        if (usuario == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado");
            return null;
        }
        req.setAttribute("usuario", usuario);
        return VISTA + "usuario-form.jsp";
    }

    /**
     * POST /usuario/guardar: solo edición (activar/desactivar y corregir
     * datos). El alta de clientes está en /registro.
     */
    private String guardarUsuario(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        Integer id = parametroEntero(req, "id");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Falta el identificador del usuario.");
            return null;
        }
        String nombre = req.getParameter("nombre");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String rol = req.getParameter("rol");
        boolean activo = "on".equals(req.getParameter("activo"))
                || "true".equals(req.getParameter("activo"));
        Rol rolEnum = "ADMIN".equals(rol) ? Rol.ADMIN : Rol.CLIENTE;

        try {
            usuarioFacade.actualizar(id, nombre, email, password, rolEnum, activo);
        } catch (ReglaNegocioException e) {
            req.setAttribute("error", e.getMessage());
            devolverValoresUsuario(req, id, nombre, email, rolEnum, activo);
            return VISTA + "usuario-form.jsp";
        }
        return redirigirConExito(req, resp, "/usuario/listar",
                "Usuario actualizado correctamente.");
    }

    /** Repuebla el formulario de usuario ante un error. */
    private void devolverValoresUsuario(HttpServletRequest req, Integer id, String nombre,
            String email, Rol rol, boolean activo) {
        Usuario temporal = new Usuario();
        temporal.setId(id);
        temporal.setNombre(nombre);
        temporal.setEmail(email);
        temporal.setRol(rol);
        temporal.setActivo(activo);
        req.setAttribute("usuario", temporal);
    }

    // ============================================================
    //  Admin: Préstamos
    // ============================================================

    private String listarPrestamos(HttpServletRequest req) {
        // Búsqueda por la persona a la que se le prestó (nombre o email)
        // o por el título del libro prestado.
        String texto = req.getParameter("q");
        long total = prestamoFacade.contarBuscar(texto);
        Pagina<Prestamo> pagina = paginar(req, total,
                n -> prestamoFacade.buscar(texto, n),
                parametrosPagina("q", texto));
        req.setAttribute("prestamos", pagina.getElementos());
        req.setAttribute("pagina", pagina);
        req.setAttribute("atrasados", prestamoFacade.contarAtrasados());
        req.setAttribute("q", texto == null ? "" : texto.trim());
        return VISTA + "prestamo-listar.jsp";
    }

    private String formPrestamo(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

            Integer id = parametroEntero(req, "id");
            Prestamo prestamo = id == null ? null : prestamoFacade.find(id);
            if (prestamo == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Préstamo no encontrado");
                return null;
            }
            req.setAttribute("prestamo", prestamo);
                
        return VISTA + "prestamo-form.jsp";
    }

    private String guardarPrestamo(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        // Con id = edición (prórroga de la fecha); sin id = alta
        Integer id = parametroEntero(req, "id");
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta el préstamo");
            return null;
        }
        Prestamo prestamo = prestamoFacade.find(id);
        if (prestamo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Préstamo no encontrado");
            return null;
        }
        LocalDate nuevaFecha;
        try {
            nuevaFecha = parametroFecha(req, "fechaDevolucionPrevista");
        } catch (DateTimeParseException e) {
            req.setAttribute("error", "La fecha de devolución no es válida.");
            req.setAttribute("prestamo", prestamo);
            return VISTA + "prestamo-form.jsp";
        }
        try {
                prestamoFacade.ajustarFechaDevolucion(id, nuevaFecha);
            
        } catch (ReglaNegocioException e) {     
            req.setAttribute("error", e.getMessage());
            req.setAttribute("prestamo", prestamo);
           
            return VISTA + "prestamo-form.jsp";
        }
        return redirigirConExito(req, resp, "/prestamo/listar",
                "Préstamo actualizado correctamente: nueva fecha de devolución registrada.");
    }

    private String eliminarPrestamo(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        Integer id = parametroEntero(req, "id");
        Prestamo prestamo = id == null ? null : prestamoFacade.find(id);
        if (prestamo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Préstamo no encontrado");
            return null;
        }
        // Si se borra un préstamo activo, se devuelve el ejemplar
        if (prestamo.getEstado() != EstadoPrestamo.DEVUELTO) {
            Libro libro = prestamo.getLibro();
            libro.setEjemplaresDisponibles(
                    Math.min(libro.getEjemplaresTotal(),
                            libro.getEjemplaresDisponibles() + 1));
            libroFacade.edit(libro);
        }
        prestamoFacade.remove(prestamo);
        return redirigirConExito(req, resp, "/prestamo/listar",
                "Préstamo eliminado correctamente.");
    }

    /** POST /prestamo/devolver: registra la devolución y genera multa si corresponde. */
    private String devolverPrestamo(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        Integer id = parametroEntero(req, "id");
        try {
            prestamoFacade.devolver(id);
        } catch (ReglaNegocioException e) {
            req.setAttribute("error", e.getMessage());
            Pagina<Prestamo> pagina = paginar(req, prestamoFacade.contar(),
                    prestamoFacade::pagina);
            req.setAttribute("prestamos", pagina.getElementos());
            req.setAttribute("pagina", pagina);
            req.setAttribute("atrasados", prestamoFacade.contarAtrasados());
            return VISTA + "prestamo-listar.jsp";
        }
        return redirigirConExito(req, resp, "/prestamo/listar",
                "Devolución registrada correctamente.");
    }

    // ============================================================
    //  Admin: Multas
    // ============================================================

    private String listarMultas(HttpServletRequest req) {
        Pagina<Multa> pagina = paginar(req, multaFacade.contarTodas(),
                multaFacade::todas);
        req.setAttribute("multas", pagina.getElementos());
        req.setAttribute("pagina", pagina);
        req.setAttribute("totalCobrar", multaFacade.sumaPendiente());
        return VISTA + "multa-listar.jsp";
    }

    /** POST (admin): marca una multa como pagada. */
    private String guardarMulta(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        Integer id = parametroEntero(req, "id");
        try {
            multaFacade.marcarPagada(id);
        } catch (ReglaNegocioException e) {
            req.setAttribute("error", e.getMessage());
            Pagina<Multa> pagina = paginar(req, multaFacade.contarTodas(),
                    multaFacade::todas);
            req.setAttribute("multas", pagina.getElementos());
            req.setAttribute("pagina", pagina);
            req.setAttribute("totalCobrar", multaFacade.sumaPendiente());
            return VISTA + "multa-listar.jsp";
        }
        return redirigirConExito(req, resp, "/multa/listar",
                "Multa marcada como pagada correctamente.");
    }

    /** POST (cliente): marca como pagada una multa propia desde "Mis préstamos". */
    private String pagarMultaCliente(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        Usuario usuario = usuarioEnSesion(req);
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        Integer id = parametroEntero(req, "id");
        try {
            Multa multa = id == null ? null : multaFacade.find(id);
            if (multa == null || multa.getPrestamo() == null
                    || multa.getPrestamo().getUsuario() == null
                    || !usuario.getId().equals(multa.getPrestamo().getUsuario().getId())) {
                req.setAttribute("error", "No puede pagar esta multa.");
                return misPrestamos(req, resp);
            }
            multaFacade.marcarPagada(id);
        } catch (ReglaNegocioException e) {
            req.setAttribute("error", e.getMessage());
            return misPrestamos(req, resp);
        }
        return redirigirConExito(req, resp, "/mis-prestamos",
                "Multa pagada correctamente.");
    }

    private String eliminarMulta(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!esPost(req)) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        Integer id = parametroEntero(req, "id");
        multaFacade.removeById(id);
        return redirigirConExito(req, resp, "/multa/listar",
                "Multa eliminada correctamente.");
    }

    // ============================================================
    //  Utilidades
    // ============================================================

    /** Extensiones admitidas para la portada. */
private static final String[] EXTENSIONES_PORTADA =
        {".jpg", ".jpeg", ".png", ".gif", ".webp"};

/**
 * Si el usuario subió un archivo, lo guarda en img/portadas/ y devuelve
 * la ruta para la columna `portada`; si no, devuelve la URL escrita.
 */
private String guardarPortada(HttpServletRequest req, String portadaUrl)
        throws ReglaNegocioException, IOException, ServletException {
    Part parte = req.getPart("archivoPortada");
    if (parte == null || parte.getSize() == 0) {
        return portadaUrl;                       // no subió nada: se usa la URL
    }
    String original = parte.getSubmittedFileName();
    if (original == null || original.isBlank()) {
        return portadaUrl;
    }
    String minusc = original.toLowerCase();
    String extension = null;
    for (String e : EXTENSIONES_PORTADA) {
        if (minusc.endsWith(e)) { extension = e; break; }
    }
    if (extension == null) {
        throw new ReglaNegocioException(
                "Formato no admitido: use JPG, PNG, GIF o WEBP.");
    }
    // Nombre generado por el servidor: evita rutas maliciosas del cliente
    String nombre = "libro-" + System.currentTimeMillis() + "-"
            + UUID.randomUUID().toString().substring(0, 8) + extension;
    String rutaCarpeta = getServletContext().getRealPath("/img/portadas");
    if (rutaCarpeta == null) {
        throw new ReglaNegocioException(
                "No se encontró la carpeta de portadas en el servidor.");
    }
    File carpeta = new File(rutaCarpeta);
    if (!carpeta.exists() && !carpeta.mkdirs()) {
        throw new ReglaNegocioException(
                "No se pudo crear la carpeta de portadas en el servidor.");
    }
    // Copia manual: Part.write() de GlassFish resuelve la ruta contra el
    // directorio de JSPs generado y falla con rutas absolutas.
    File destino = new File(carpeta, nombre);
    try (InputStream entrada = parte.getInputStream();
         OutputStream salida = new FileOutputStream(destino)) {
        entrada.transferTo(salida);
    }
    return req.getContextPath() + "/img/portadas/" + nombre;
}
    
    private boolean esPost(HttpServletRequest req) {
        return "POST".equals(req.getMethod());
    }

    private Usuario usuarioEnSesion(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        return sesion == null ? null : (Usuario) sesion.getAttribute("usuario");
    }

    /** Lee un parámetro entero; devuelve null si está vacío o no es numérico. */
    private Integer parametroEntero(HttpServletRequest req, String nombre) {
        String valor = req.getParameter(nombre);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;        // no es un número: se trata como ausente
        }
    }

    /** Lee un parámetro entero con valor por defecto. */
    private int parametroEnteroDefecto(HttpServletRequest req, String nombre, int defecto) {
        Integer valor = parametroEntero(req, nombre);
        return valor == null ? defecto : valor;
    }

    /** Lee un parámetro de fecha (yyyy-MM-dd); null si está vacío. */
    private LocalDate parametroFecha(HttpServletRequest req, String nombre) {
        String valor = req.getParameter(nombre);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return LocalDate.parse(valor.trim());
    }

    // --------------------------------------------------------
    //  Paginación de los listados
    // --------------------------------------------------------

    /**
     * Trae la página pedida en la URL (?pagina=) de una consulta paginada.
     * La página se ajusta al rango válido (1..totalPaginas) y se prepara la
     * información que dibuja el fragmento paginacion.jspf.
     *
     * @param total     total de registros de la consulta
     * @param buscador  trae los elementos de la página indicada
     */
    private <T> Pagina<T> paginar(HttpServletRequest req, long total,
            IntFunction<List<T>> buscador) {
        return paginar(req, total, buscador, "");
    }

    /**
     * Igual que el anterior conservando parámetros extra en los enlaces
     * (por ejemplo la búsqueda y el filtro del catálogo).
     */
    private <T> Pagina<T> paginar(HttpServletRequest req, long total,
            IntFunction<List<T>> buscador, String parametros) {
        int pagina = Pagina.ajustar(parametroEntero(req, "pagina"), total);
        return Pagina.de(buscador.apply(pagina), total, pagina,
                req.getContextPath() + req.getServletPath(), parametros);
    }

    /** Convierte pares nombre/valor en "&a=1&b=2", ya codificados para la URL. */
    private String parametrosPagina(String... pares) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i + 1 < pares.length; i += 2) {
            String valor = pares[i + 1];
            if (valor == null || valor.isBlank()) {
                continue;
            }
            sb.append('&').append(pares[i]).append('=')
              .append(URLEncoder.encode(valor.trim(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
