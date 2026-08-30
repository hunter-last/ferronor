package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.ventas.modelo.EstadoComprobante;
import com.ferronor.sic.ventas.modelo.EstadoVenta;
import com.ferronor.sic.ventas.modelo.Venta;
import com.ferronor.sic.ventas.modelo.dto.VentaConsulta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
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

    @Override
    public List<VentaConsulta> consultarHistorial(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer idCliente,
            EstadoVenta estado,
            Integer idTipoComprobante) {

        StringBuilder sql = new StringBuilder(
                "SELECT "
                + "v.id_venta, "
                + "v.fecha, "
                + "v.id_cliente, "
                + "cl.nombre_razon_social, "
                + "cl.tipo_documento, "
                + "cl.numero_documento, "
                + "v.id_forma_pago, "
                + "fp.nombre AS nombre_forma_pago, "
                + "v.estado, "
                + "v.subtotal, "
                + "v.igv, "
                + "v.total, "
                + "v.id_usuario, "
                + "u.nombres || ' ' || u.apellidos AS nombre_usuario, "
                + "c.id_comprobante, "
                + "tc.nombre AS nombre_tipo_comprobante, "
                + "c.serie, "
                + "c.numero, "
                + "c.fecha_emision, "
                + "c.estado AS estado_comprobante "
                + "FROM venta v "
                + "JOIN cliente cl "
                + "ON cl.id_cliente = v.id_cliente "
                + "JOIN forma_pago fp "
                + "ON fp.id_forma_pago = v.id_forma_pago "
                + "JOIN usuario u "
                + "ON u.id_usuario = v.id_usuario "
                + "LEFT JOIN comprobante c "
                + "ON c.id_venta = v.id_venta "
                + "LEFT JOIN tipo_comprobante tc "
                + "ON tc.id_tipo_comprobante = c.id_tipo_comprobante "
                + "WHERE 1 = 1 "
        );

        if (fechaDesde != null) {
            sql.append("AND v.fecha >= ? ");
        }

        if (fechaHasta != null) {
            sql.append("AND v.fecha < ? ");
        }

        if (idCliente != null) {
            sql.append("AND v.id_cliente = ? ");
        }

        if (estado != null) {
            sql.append("AND v.estado = ? ");
        }

        if (idTipoComprobante != null) {
            sql.append("AND c.id_tipo_comprobante = ? ");
        }

        sql.append("ORDER BY v.fecha DESC, v.id_venta DESC");

        Connection cn = obtenerConexion();
        List<VentaConsulta> resultado = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            int i = 1;

            if (fechaDesde != null) {
                ps.setTimestamp(
                        i++,
                        Timestamp.valueOf(fechaDesde.atStartOfDay())
                );
            }

            if (fechaHasta != null) {
                ps.setTimestamp(
                        i++,
                        Timestamp.valueOf(
                                fechaHasta.plusDays(1).atStartOfDay()
                        )
                );
            }

            if (idCliente != null) {
                ps.setInt(i++, idCliente);
            }

            if (estado != null) {
                ps.setString(i++, estado.name());
            }

            if (idTipoComprobante != null) {
                ps.setInt(i++, idTipoComprobante);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    resultado.add(mapearConsulta(rs));
                }
            }

            return resultado;

        } catch (SQLException e) {
            throw error(
                    "Error al consultar historial de ventas",
                    e
            );
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

    private VentaConsulta mapearConsulta(ResultSet rs)
            throws SQLException {

        Timestamp fechaEmision
                = rs.getTimestamp("fecha_emision");

        String estadoComprobante
                = rs.getString("estado_comprobante");

        return new VentaConsulta(
                rs.getInt("id_venta"),
                rs.getTimestamp("fecha").toLocalDateTime(),
                rs.getInt("id_cliente"),
                rs.getString("nombre_razon_social"),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento"),
                rs.getInt("id_forma_pago"),
                rs.getString("nombre_forma_pago"),
                EstadoVenta.valueOf(
                        rs.getString("estado")
                ),
                rs.getBigDecimal("subtotal"),
                rs.getBigDecimal("igv"),
                rs.getBigDecimal("total"),
                rs.getInt("id_usuario"),
                rs.getString("nombre_usuario"),
                rs.getInt("id_comprobante"),
                rs.getString("nombre_tipo_comprobante"),
                rs.getString("serie"),
                rs.getString("numero"),
                fechaEmision != null
                        ? fechaEmision.toLocalDateTime()
                        : null,
                estadoComprobante != null
                        ? EstadoComprobante.valueOf(
                                estadoComprobante
                        )
                        : null
        );
    }
}
