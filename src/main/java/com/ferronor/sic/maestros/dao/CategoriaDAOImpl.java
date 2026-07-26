package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.maestros.modelo.Categoria;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOImpl extends AbstractDAO implements CategoriaDAO {

    private static final String TABLA = "categoria";
    private static final String COLUMNAS = "id_categoria, nombre";

    @Override
    public void insertar(Categoria categoria) {
        String sql = "INSERT INTO " + TABLA + " (nombre) VALUES (?) RETURNING id_categoria";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la categoría");
                }
                categoria.setIdCategoria(rs.getInt("id_categoria"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar categoría", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(Categoria categoria) {
        String sql = "UPDATE " + TABLA + " SET nombre = ? WHERE id_categoria = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getIdCategoria());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe categoría con id " + categoria.getIdCategoria());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar categoría " + categoria.getIdCategoria(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Categoria buscarPorId(Integer id) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_categoria = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar categoría " + id, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Categoria> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY nombre";
        Connection cn = obtenerConexion();
        List<Categoria> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar categorías", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Categoria buscarPorNombre(String nombre) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE UPPER(nombre) = UPPER(?)";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar categoría por nombre", e);
        } finally {
            cerrar(cn);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria(rs.getString("nombre"));
        c.setIdCategoria(rs.getInt("id_categoria"));
        return c;
    }
}
