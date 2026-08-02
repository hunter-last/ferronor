package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.ventas.modelo.EstadoVenta;
import com.ferronor.sic.ventas.modelo.Venta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VentaDAOImpl extends AbstractDAO implements VentaDAO {

    private static final String TABLA = "venta";
    private static final String COLUMNAS
            = "id_venta, id_cliente, fecha, id_forma_pago, estado, subtotal, igv, total, id_usuario";

    @Override
    public void insertar(Venta venta) {
        String sql = "INSERT INTO " + TABLA
                + " (id_cliente, fecha, id_forma_pago, estado, subtotal, igv, total, id_usuario) "
                + "VALUES (?, now(), ?, ?, ?, ?, ?, ?) RETURNING id_venta, fecha";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, venta.getIdCliente());
            ps.setInt(2, venta.getIdFormaPago());
            ps.setString(3, venta.getEstado().name());
            ps.setBigDecimal(4, venta.getSubtotal());
            ps.setBigDecimal(5, venta.getIgv());
            ps.setBigDecimal(6, venta.getTotal());
            ps.setInt(7, venta.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la venta");
                }
                venta.setIdVenta(rs.getInt("id_venta"));
                venta.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar venta", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void cambiarEstado(int idVenta, EstadoVenta estado) {
        String sql = "UPDATE " + TABLA + " SET estado = ? WHERE id_venta = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe venta con id " + idVenta);
                }
            }
        } catch (SQLException e) {
            throw error("Error al cambiar estado de la venta " + idVenta, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Venta buscarPorId(Integer idVenta) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_venta = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar venta " + idVenta, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Venta> listar() {
        Connection cn = obtenerConexion();
        List<Venta> resultado = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha DESC";
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar ventas", e);
        } finally {
            cerrar(cn);
        }
    }

    private Venta mapear(ResultSet rs) throws SQLException {
        Venta venta = new Venta();
        venta.setIdVenta(rs.getInt("id_venta"));
        venta.setIdCliente(rs.getInt("id_cliente"));
        venta.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        venta.setIdFormaPago(rs.getInt("id_forma_pago"));
        venta.setEstado(EstadoVenta.valueOf(rs.getString("estado")));
        venta.setSubtotal(rs.getBigDecimal("subtotal"));
        venta.setIgv(rs.getBigDecimal("igv"));
        venta.setTotal(rs.getBigDecimal("total"));
        venta.setIdUsuario(rs.getInt("id_usuario"));
        return venta;
    }
}