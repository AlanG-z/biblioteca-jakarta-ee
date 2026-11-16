package com.mycompany.biblioteca.sesiones;

import com.mycompany.biblioteca.Configuracion;
import com.mycompany.biblioteca.Pagina;
import com.mycompany.biblioteca.ReglaNegocioException;
import com.mycompany.biblioteca.Rol;
import com.mycompany.biblioteca.Usuario;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Facade de usuarios: CRUD, autenticación con BCrypt y control de
 * préstamos asociados antes de eliminar.
 */
@Stateless
public class UsuarioFacade extends AbstractFacade<Usuario> {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager em;

    public UsuarioFacade() {
        super(Usuario.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /** Da de alta un usuario con la contraseña hasheada con BCrypt. */
    public void crear(String nombre, String email, String password, Rol rol, boolean activo)
            throws ReglaNegocioException {
        String tNombre = nombre == null ? "" : nombre.trim();
        String tEmail = email == null ? "" : email.trim().toLowerCase();
        if (tNombre.isEmpty()) {
            throw new ReglaNegocioException("El nombre es obligatorio.");
        }
        validarLongitudes(tNombre, tEmail);
        if (!tEmail.matches(Configuracion.PATRON_EMAIL)) {
            throw new ReglaNegocioException("El email no tiene un formato válido.");
        }
        if (password == null || password.length() < Configuracion.PASSWORD_MINIMO) {
            throw new ReglaNegocioException(
                    "La contraseña debe tener al menos " + Configuracion.PASSWORD_MINIMO + " caracteres.");
        }
        if (buscarPorEmail(tEmail) != null) {
            throw new ReglaNegocioException("Ya existe un usuario con ese email.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(tNombre);
        usuario.setEmail(tEmail);
        usuario.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        usuario.setRol(rol == null ? Rol.CLIENTE : rol);
        usuario.setActivo(activo);
        create(usuario);
    }

    /**
     * Modifica un usuario. Si password es vacío/null se conserva la
     * contraseña anterior.
     */
    public void actualizar(Integer id, String nombre, String email, String password,
            Rol rol, boolean activo) throws ReglaNegocioException {
        Usuario usuario = find(id);
        if (usuario == null) {
            throw new ReglaNegocioException("El usuario no existe.");
        }
        String tNombre = nombre == null ? "" : nombre.trim();
        String tEmail = email == null ? "" : email.trim().toLowerCase();
        if (tNombre.isEmpty()) {
            throw new ReglaNegocioException("El nombre es obligatorio.");
        }
        validarLongitudes(tNombre, tEmail);
        if (!tEmail.matches(Configuracion.PATRON_EMAIL)) {
            throw new ReglaNegocioException("El email no tiene un formato válido.");
        }
        Usuario duplicado = buscarPorEmail(tEmail);
        if (duplicado != null && !duplicado.getId().equals(id)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese email.");
        }
        if (password != null && !password.isEmpty()
                && password.length() < Configuracion.PASSWORD_MINIMO) {
            throw new ReglaNegocioException(
                    "La contraseña debe tener al menos " + Configuracion.PASSWORD_MINIMO + " caracteres.");
        }

        usuario.setNombre(tNombre);
        usuario.setEmail(tEmail);
        if (password != null && !password.isEmpty()) {
            usuario.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        }
        if (rol != null) {
            usuario.setRol(rol);
        }
        usuario.setActivo(activo);
        edit(usuario);
    }

    /**
     * Elimina un usuario. Regla de negocio: no se puede eliminar si
     * tiene préstamos asociados.
     */
    public void eliminar(Integer id) throws ReglaNegocioException {
        Usuario usuario = find(id);
        if (usuario == null) {
            throw new ReglaNegocioException("El usuario no existe.");
        }
        Long prestamos = em.createQuery(
                "SELECT COUNT(p) FROM Prestamo p WHERE p.usuario.id = :idUsuario", Long.class)
                .setParameter("idUsuario", id)
                .getSingleResult();
        if (prestamos > 0) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el usuario: tiene " + prestamos
                    + " préstamo(s) asociado(s). Resuelva primero esos préstamos.");
        }
        remove(usuario);
    }

    /**
     * Capa de negocio de longitud: evita el error 500 que produciría
     * exceder el tamaño de la columna en la base de datos.
     */
    private void validarLongitudes(String nombre, String email)
            throws ReglaNegocioException {
        if (nombre.length() > Configuracion.NOMBRE_MAXIMO) {
            throw new ReglaNegocioException("El nombre no puede superar "
                    + Configuracion.NOMBRE_MAXIMO + " caracteres.");
        }
        if (email.length() > Configuracion.EMAIL_MAXIMO) {
            throw new ReglaNegocioException("El email no puede superar "
                    + Configuracion.EMAIL_MAXIMO + " caracteres.");
        }
    }

    /**
     * Valida email y contraseña contra la tabla usuarios.
     * Devuelve el usuario si es correcto y está activo, o null si no.
     */
    public Usuario autenticar(String email, String password) {
        if (email == null || password == null) {
            return null;
        }
        Usuario usuario = buscarPorEmail(email.trim().toLowerCase());
        if (usuario == null || !usuario.isActivo()) {
            return null;
        }
        try {
            if (BCrypt.checkpw(password, usuario.getPasswordHash())) {
                return usuario;
            }
        } catch (IllegalArgumentException e) {
            // Hash corrupto: se trata como credencial inválida
            return null;
        }
        return null;
    }

    /** Busca un usuario por su email (ya en minúsculas). */
    public Usuario buscarPorEmail(String email) {
        List<Usuario> resultados = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
                .setParameter("email", email.trim().toLowerCase())
                .getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    /** Página del listado de usuarios (orden alfabético). */
    public List<Usuario> pagina(int numero) {
        return em.createQuery("SELECT u FROM Usuario u ORDER BY u.nombre", Usuario.class)
                .setFirstResult(Pagina.desplazamiento(numero))
                .setMaxResults(Configuracion.PAGINA_TAMANO)
                .getResultList();
    }

    /** Total de usuarios: marca el número de páginas del listado. */
    public long contar() {
        return em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class)
                .getSingleResult();
    }

    /** Cuentas con rol ADMIN (resumen del listado). */
    public long contarAdministradores() {
        Long total = em.createQuery(
                "SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol", Long.class)
                .setParameter("rol", Rol.ADMIN)
                .getSingleResult();
        return total == null ? 0 : total;
    }
}
