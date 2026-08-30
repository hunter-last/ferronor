package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.ventas.modelo.DetalleVenta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleVentaDAOImpl extends AbstractDAO implements DetalleVentaDAO {

    private static final String TABLA = "detalle_venta";
    private static final String COLUMNAS
            = "id_detalle, id_venta, id_producto, cantidad, precio_unitario, subtotal";

    @Override
    public void insertar(DetalleVenta detalle) {
        String sql = "INSERT INTO " + TABLA + " (id_venta, id_producto, cantidad, precio_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id_detalle";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdVenta());
            ps.setInt(2, detalle.getIdProducto());
            ps.setBigDecimal(3, detalle.getCantidad());
            ps.setBigDecimal(4, detalle.getPrecioUnitario());
            ps.setBigDecimal(5, detalle.getSubtotal());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el detalle de venta");
                }
                detalle.setIdDetalle(rs.getInt("id_detalle"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar detalle de venta", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public DetalleVenta buscarPorId(Integer idDetalle) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_detalle = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar detalle de venta " + idDetalle, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<DetalleVenta> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA);
    }

    @Override
    public List<DetalleVenta> listarPorVenta(int idVenta) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_venta = ? ORDER BY id_detalle";
        List<DetalleVenta> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar detalles de la venta " + idVenta, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<DetalleVenta> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<DetalleVenta> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar detalles de venta", e);
        } finally {
            cerrar(cn);
        }
    }

    private DetalleVenta mapear(ResultSet rs) throws SQLException {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setIdDetalle(rs.getInt("id_detalle"));
        detalle.setIdVenta(rs.getInt("id_venta"));
        detalle.setIdProducto(rs.getInt("id_producto"));
        detalle.setCantidad(rs.getBigDecimal("cantidad"));
        detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        detalle.setSubtotal(rs.getBigDecimal("subtotal"));
        return detalle;
    }
}