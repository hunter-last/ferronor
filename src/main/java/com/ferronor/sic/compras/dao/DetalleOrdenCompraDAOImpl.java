package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.DetalleOrdenCompra;
import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleOrdenCompraDAOImpl extends AbstractDAO implements DetalleOrdenCompraDAO {

    private static final String TABLA = "detalle_orden_compra";
    private static final String COLUMNAS = "id_detalle, id_orden_compra, id_producto, cantidad";

    @Override
    public void insertar(DetalleOrdenCompra detalle) {
        String sql = "INSERT INTO " + TABLA + " (id_orden_compra, id_producto, cantidad) "
                + "VALUES (?, ?, ?) RETURNING id_detalle";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdOrdenCompra());
            ps.setInt(2, detalle.getIdProducto());
            ps.setBigDecimal(3, detalle.getCantidad());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el detalle de la orden de compra");
                }
                detalle.setIdDetalle(rs.getInt("id_detalle"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar detalle de orden de compra", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public DetalleOrdenCompra buscarPorId(Integer idDetalle) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_detalle = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar detalle de orden de compra " + idDetalle, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<DetalleOrdenCompra> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA);
    }

    @Override
    public List<DetalleOrdenCompra> listarPorOrdenCompra(int idOrdenCompra) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_orden_compra = ? ORDER BY id_detalle";
        List<DetalleOrdenCompra> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idOrdenCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar detalles de la orden de compra " + idOrdenCompra, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<DetalleOrdenCompra> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<DetalleOrdenCompra> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar detalles de orden de compra", e);
        } finally {
            cerrar(cn);
        }
    }

    private DetalleOrdenCompra mapear(ResultSet rs) throws SQLException {
        DetalleOrdenCompra detalle = new DetalleOrdenCompra();
        detalle.setIdDetalle(rs.getInt("id_detalle"));
        detalle.setIdOrdenCompra(rs.getInt("id_orden_compra"));
        detalle.setIdProducto(rs.getInt("id_producto"));
        detalle.setCantidad(rs.getBigDecimal("cantidad"));
        return detalle;
    }
}