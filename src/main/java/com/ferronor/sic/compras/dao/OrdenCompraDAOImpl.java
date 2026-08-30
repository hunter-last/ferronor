package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.EstadoOrdenCompra;
import com.ferronor.sic.compras.modelo.OrdenCompra;
import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrdenCompraDAOImpl extends AbstractDAO implements OrdenCompraDAO {

    private static final String TABLA = "orden_compra";
    private static final String COLUMNAS
            = "id_orden_compra, id_proveedor, fecha, estado, id_usuario_solicita, id_usuario_aprueba, fecha_aprobacion";

    @Override
    public void insertar(OrdenCompra orden) {
        String sql = "INSERT INTO " + TABLA + " (id_proveedor, fecha, estado, id_usuario_solicita) "
                + "VALUES (?, now(), ?, ?) RETURNING id_orden_compra, fecha";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, orden.getIdProveedor());
            ps.setString(2, orden.getEstado().name());
            ps.setInt(3, orden.getIdUsuarioSolicita());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la orden de compra");
                }
                orden.setIdOrdenCompra(rs.getInt("id_orden_compra"));
                orden.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar orden de compra", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(OrdenCompra orden) {
        // Una orden de compra solo cambia de estado (aprobar/rechazar), ver cambiarEstado().
        // No se reescriben proveedor/detalles después de creada.
        throw new UnsupportedOperationException(
                "OrdenCompra no se actualiza directamente; usar cambiarEstado(...)");
    }

    @Override
    public void cambiarEstado(int idOrdenCompra, EstadoOrdenCompra estado, int idUsuarioAprueba) {
        String sql = "UPDATE " + TABLA + " SET estado = ?, id_usuario_aprueba = ?, fecha_aprobacion = now() "
                + "WHERE id_orden_compra = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, idUsuarioAprueba);
            ps.setInt(3, idOrdenCompra);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe orden de compra con id " + idOrdenCompra);
                }
            }
        } catch (SQLException e) {
            throw error("Error al cambiar estado de la orden de compra " + idOrdenCompra, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<OrdenCompra> listarDisponiblesParaCompra() {

        String sql = "SELECT " + COLUMNAS
                + " FROM " + TABLA
                + " WHERE estado = ? "
                + "AND NOT EXISTS ("
                + "    SELECT 1 "
                + "    FROM compra c "
                + "    WHERE c.id_orden_compra = orden_compra.id_orden_compra"
                + ") "
                + "ORDER BY fecha DESC";

        Connection cn = obtenerConexion();
        List<OrdenCompra> resultado = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, EstadoOrdenCompra.APROBADA.name());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }

            return resultado;

        } catch (SQLException e) {
            throw error(
                    "Error al listar órdenes de compra disponibles para registrar compras",
                    e
            );
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public OrdenCompra buscarPorId(Integer idOrdenCompra) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_orden_compra = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idOrdenCompra);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar orden de compra " + idOrdenCompra, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<OrdenCompra> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha DESC");
    }

    @Override
    public List<OrdenCompra> listarPorEstado(EstadoOrdenCompra estado) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE estado = ? ORDER BY fecha DESC";
        List<OrdenCompra> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar órdenes de compra por estado " + estado, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<OrdenCompra> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<OrdenCompra> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar órdenes de compra", e);
        } finally {
            cerrar(cn);
        }
    }

    private OrdenCompra mapear(ResultSet rs) throws SQLException {
        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrdenCompra(rs.getInt("id_orden_compra"));
        orden.setIdProveedor(rs.getInt("id_proveedor"));
        orden.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        orden.setEstado(EstadoOrdenCompra.valueOf(rs.getString("estado")));
        orden.setIdUsuarioSolicita(rs.getInt("id_usuario_solicita"));

        int idUsuarioAprueba = rs.getInt("id_usuario_aprueba");
        orden.setIdUsuarioAprueba(rs.wasNull() ? null : idUsuarioAprueba);

        Timestamp fechaAprobacion = rs.getTimestamp("fecha_aprobacion");
        orden.setFechaAprobacion(fechaAprobacion != null ? fechaAprobacion.toLocalDateTime() : null);

        return orden;
    }
}
