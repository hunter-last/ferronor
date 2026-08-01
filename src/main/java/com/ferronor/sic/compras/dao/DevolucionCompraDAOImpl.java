package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.DevolucionCompra;
import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DevolucionCompraDAOImpl extends AbstractDAO implements DevolucionCompraDAO {

    private static final String TABLA = "devolucion_compra";
    private static final String COLUMNAS
            = "id_devolucion, id_compra, id_producto, cantidad, motivo, fecha, id_usuario";

    @Override
    public void insertar(DevolucionCompra dev) {
        String sql = "INSERT INTO " + TABLA + " (id_compra, id_producto, cantidad, motivo, fecha, id_usuario) "
                + "VALUES (?, ?, ?, ?, now(), ?) RETURNING id_devolucion, fecha";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, dev.getIdCompra());
            ps.setInt(2, dev.getIdProducto());
            ps.setBigDecimal(3, dev.getCantidad());
            ps.setString(4, dev.getMotivo());
            ps.setInt(5, dev.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la devolución de compra");
                }
                dev.setIdDevolucion(rs.getInt("id_devolucion"));
                dev.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar devolución de compra", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public DevolucionCompra buscarPorId(Integer idDevolucion) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_devolucion = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDevolucion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar devolución de compra " + idDevolucion, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<DevolucionCompra> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha DESC");
    }

    @Override
    public List<DevolucionCompra> listarPorCompra(int idCompra) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_compra = ? ORDER BY fecha DESC";
        List<DevolucionCompra> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar devoluciones de la compra " + idCompra, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<DevolucionCompra> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<DevolucionCompra> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar devoluciones de compra", e);
        } finally {
            cerrar(cn);
        }
    }

    private DevolucionCompra mapear(ResultSet rs) throws SQLException {
        DevolucionCompra dev = new DevolucionCompra();
        dev.setIdDevolucion(rs.getInt("id_devolucion"));
        dev.setIdCompra(rs.getInt("id_compra"));
        dev.setIdProducto(rs.getInt("id_producto"));
        dev.setCantidad(rs.getBigDecimal("cantidad"));
        dev.setMotivo(rs.getString("motivo"));
        dev.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        dev.setIdUsuario(rs.getInt("id_usuario"));
        return dev;
    }
}