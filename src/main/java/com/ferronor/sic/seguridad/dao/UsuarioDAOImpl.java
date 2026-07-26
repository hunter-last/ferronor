package com.ferronor.sic.seguridad.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.seguridad.modelo.Usuario;
import com.ferronor.sic.shared.dao.AbstractDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl extends AbstractDAO implements UsuarioDAO {

    private static final String TABLA = "usuario";

    private static final String COLUMNAS
            = "id_usuario, nombres, apellidos, usuario_login, password_hash, id_rol, activo, fecha_creacion";

    @Override
    public void insertar(Usuario u) {
        String sql = "INSERT INTO " + TABLA
                + " (nombres, apellidos, usuario_login, password_hash, id_rol, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_usuario";

        Connection cn = obtenerConexion();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getUsuarioLogin());
            ps.setString(4, u.getPasswordHash());
            ps.setInt(5, u.getIdRol());
            ps.setBoolean(6, u.isActivo());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el usuario");
                }
                u.setIdUsuario(rs.getInt("id_usuario"));
            }

        } catch (SQLException e) {
            throw error("Error al insertar usuario", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(Usuario u) {

        String sql
                = "UPDATE " + TABLA
                + " SET nombres = ?, "
                + "apellidos = ?, "
                + "usuario_login = ?, "
                + "password_hash = ?, "
                + "id_rol = ?, "
                + "activo = ? "
                + "WHERE id_usuario = ? RETURNING 1";

        Connection cn = obtenerConexion();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getUsuarioLogin());
            ps.setString(4, u.getPasswordHash());
            ps.setInt(5, u.getIdRol());
            ps.setBoolean(6, u.isActivo());
            ps.setInt(7, u.getIdUsuario());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException(
                            "No existe usuario con id " + u.getIdUsuario());
                }
            }

        } catch (SQLException e) {
            throw error("Error al actualizar usuario " + u.getIdUsuario(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void activar(int idUsuario) {
        cambiarEstado(idUsuario, true);
    }

    @Override
    public void desactivar(int idUsuario) {
        cambiarEstado(idUsuario, false);
    }

    private void cambiarEstado(int idUsuario, boolean activo) {

        String sql
                = "UPDATE " + TABLA
                + " SET activo = ? "
                + "WHERE id_usuario = ? RETURNING 1";

        Connection cn = obtenerConexion();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException(
                            "No existe usuario con id " + idUsuario);
                }
            }

        } catch (SQLException e) {
            throw error(
                    "Error al cambiar estado del usuario " + idUsuario, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Usuario buscarPorId(Integer id) {
        return buscarPorCampo("id_usuario = ?", id);
    }

    @Override
    public Usuario buscarPorLogin(String usuarioLogin) {
        return buscarPorCampo(
                "usuario_login = ?",
                usuarioLogin.trim()
        );
    }

    private Usuario buscarPorCampo(String condicion, Object valor) {

        String sql
                = "SELECT " + COLUMNAS
                + " FROM " + TABLA
                + " WHERE " + condicion;

        Connection cn = obtenerConexion();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setObject(1, valor);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw error("Error al buscar usuario", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Usuario> listar() {

        return listarConFiltro(
                "SELECT " + COLUMNAS
                + " FROM " + TABLA
                + " ORDER BY apellidos, nombres"
        );
    }

    private List<Usuario> listarConFiltro(String sql) {

        Connection cn = obtenerConexion();

        List<Usuario> resultado = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapear(rs));
            }

            return resultado;

        } catch (SQLException e) {
            throw error("Error al listar usuarios", e);
        } finally {
            cerrar(cn);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {

        Usuario u = new Usuario(
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("usuario_login"),
                rs.getString("password_hash"),
                rs.getInt("id_rol")
        );

        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setActivo(rs.getBoolean("activo"));

        Timestamp ts = rs.getTimestamp("fecha_creacion");
        if (ts != null) {
            u.setFechaCreacion(ts.toLocalDateTime());
        }

        return u;
    }

    @Override
    public void actualizarPassword(int idUsuario, String passwordHash) {
        String sql = "UPDATE " + TABLA + " SET password_hash = ? WHERE id_usuario = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe usuario con id " + idUsuario);
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar contraseña del usuario " + idUsuario, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Usuario> listarActivos() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE activo = TRUE ORDER BY apellidos, nombres");
    }

}
