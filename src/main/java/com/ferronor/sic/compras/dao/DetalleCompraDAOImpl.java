package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.DetalleCompra;
import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DetalleCompraDAOImpl extends AbstractDAO implements DetalleCompraDAO {

    private static final String TABLA = "detalle_compra";
    private static final String COLUMNAS
            = "id_detalle, id_compra, id_producto, cantidad, costo_unitario, subtotal";

    @Override
    public void insertar(DetalleCompra detalle) {
        String sql = "INSERT INTO " + TABLA + " (id_compra, id_producto, cantidad, costo_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id_detalle";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, detalle.getIdCompra());
            ps.setInt(2, detalle.getIdProducto());
            ps.setBigDecimal(3, detalle.getCantidad());
            ps.setBigDecimal(4, detalle.getCostoUnitario());
            ps.setBigDecimal(5, detalle.getSubtotal());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el detalle de compra");
                }
                detalle.setIdDetalle(rs.getInt("id_detalle"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar detalle de compra", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public DetalleCompra buscarPorId(Integer idDetalle) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_detalle = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar detalle de compra " + idDetalle, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<DetalleCompra> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA);
    }

    @Override
    public List<DetalleCompra> listarPorCompra(int idCompra) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_compra = ? ORDER BY id_detalle";
        List<DetalleCompra> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar detalles de la compra " + idCompra, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<DetalleCompra> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<DetalleCompra> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar detalles de compra", e);
        } finally {
            cerrar(cn);
        }
    }

    private DetalleCompra mapear(ResultSet rs) throws SQLException {
        DetalleCompra detalle = new DetalleCompra();
        detalle.setIdDetalle(rs.getInt("id_detalle"));
        detalle.setIdCompra(rs.getInt("id_compra"));
        detalle.setIdProducto(rs.getInt("id_producto"));
        detalle.setCantidad(rs.getBigDecimal("cantidad"));
        detalle.setCostoUnitario(rs.getBigDecimal("costo_unitario"));
        detalle.setSubtotal(rs.getBigDecimal("subtotal"));
        return detalle;
    }
}