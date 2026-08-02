package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.ventas.modelo.DevolucionVenta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DevolucionVentaDAOImpl extends AbstractDAO implements DevolucionVentaDAO {

    private static final String TABLA = "devolucion_venta";
    private static final String COLUMNAS
            = "id_devolucion, id_venta, id_producto, cantidad, motivo, fecha, id_usuario";

    @Override
    public void insertar(DevolucionVenta dev) {
        String sql = "INSERT INTO " + TABLA + " (id_venta, id_producto, cantidad, motivo, fecha, id_usuario) "
                + "VALUES (?, ?, ?, ?, now(), ?) RETURNING id_devolucion, fecha";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, dev.getIdVenta());
            ps.setInt(2, dev.getIdProducto());
            ps.setBigDecimal(3, dev.getCantidad());
            ps.setString(4, dev.getMotivo());
            ps.setInt(5, dev.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la devolución de venta");
                }
                dev.setIdDevolucion(rs.getInt("id_devolucion"));
                dev.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar devolución de venta", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public DevolucionVenta buscarPorId(Integer idDevolucion) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_devolucion = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDevolucion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar devolución de venta " + idDevolucion, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<DevolucionVenta> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha DESC");
    }

    @Override
    public List<DevolucionVenta> listarPorVenta(int idVenta) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_venta = ? ORDER BY fecha DESC";
        List<DevolucionVenta> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar devoluciones de la venta " + idVenta, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<DevolucionVenta> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<DevolucionVenta> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar devoluciones de venta", e);
        } finally {
            cerrar(cn);
        }
    }

    private DevolucionVenta mapear(ResultSet rs) throws SQLException {
        DevolucionVenta dev = new DevolucionVenta();
        dev.setIdDevolucion(rs.getInt("id_devolucion"));
        dev.setIdVenta(rs.getInt("id_venta"));
        dev.setIdProducto(rs.getInt("id_producto"));
        dev.setCantidad(rs.getBigDecimal("cantidad"));
        dev.setMotivo(rs.getString("motivo"));
        dev.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        dev.setIdUsuario(rs.getInt("id_usuario"));
        return dev;
    }
}