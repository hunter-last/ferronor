package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOImpl extends AbstractDAO implements ProductoDAO {

    private static final String TABLA = "producto";
    private static final String COLUMNAS
            = "id_producto, codigo, nombre, id_categoria, id_unidad_medida, stock_minimo, precio_venta, activo";

    @Override
    public void insertar(Producto p) {
        String sql = "INSERT INTO " + TABLA
                + " (codigo, nombre, id_categoria, id_unidad_medida, stock_minimo, precio_venta, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_producto";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getIdCategoria());
            ps.setInt(4, p.getIdUnidadMedida());
            ps.setBigDecimal(5, p.getStockMinimo());
            ps.setBigDecimal(6, p.getPrecioVenta());
            ps.setBoolean(7, p.isActivo());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el producto");
                }
                p.setIdProducto(rs.getInt("id_producto"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar producto", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(Producto p) {
        String sql = "UPDATE " + TABLA + " SET codigo = ?, nombre = ?, id_categoria = ?, id_unidad_medida = ?, "
                + "stock_minimo = ?, precio_venta = ?, activo = ? WHERE id_producto = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getIdCategoria());
            ps.setInt(4, p.getIdUnidadMedida());
            ps.setBigDecimal(5, p.getStockMinimo());
            ps.setBigDecimal(6, p.getPrecioVenta());
            ps.setBoolean(7, p.isActivo());
            ps.setInt(8, p.getIdProducto());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe producto con id " + p.getIdProducto());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar producto " + p.getIdProducto(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void activar(int idProducto) {
        cambiarEstado(idProducto, true);
    }

    @Override
    public void desactivar(int idProducto) {
        cambiarEstado(idProducto, false);
    }

    private void cambiarEstado(int idProducto, boolean activo) {
        String sql = "UPDATE " + TABLA + " SET activo = ? WHERE id_producto = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe producto con id " + idProducto);
                }
            }
        } catch (SQLException e) {
            throw error("Error al cambiar estado del producto " + idProducto, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Producto buscarPorId(Integer id) {
        return buscarPorCampo("id_producto = ?", id);
    }

    @Override
    public Producto buscarPorCodigo(String codigo) {
        return buscarPorCampo("codigo = ?", codigo);
    }

    private Producto buscarPorCampo(String condicion, Object valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar producto", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Producto> listar() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY nombre");
    }

    @Override
    public List<Producto> listarActivos() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE activo = TRUE ORDER BY nombre");
    }

    private List<Producto> listarConFiltro(String sql) {
        Connection cn = obtenerConexion();
        List<Producto> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar productos", e);
        } finally {
            cerrar(cn);
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto(rs.getString("codigo"), rs.getString("nombre"),
                rs.getInt("id_categoria"), rs.getInt("id_unidad_medida"),
                rs.getBigDecimal("stock_minimo"), rs.getBigDecimal("precio_venta"));
        p.setIdProducto(rs.getInt("id_producto"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}
