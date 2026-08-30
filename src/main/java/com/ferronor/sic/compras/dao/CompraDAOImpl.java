package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.Compra;
import com.ferronor.sic.compras.modelo.dto.CompraConsulta;
import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CompraDAOImpl extends AbstractDAO implements CompraDAO {

    private static final String TABLA = "compra";
    private static final String COLUMNAS
            = "id_compra, id_orden_compra, id_proveedor, fecha, id_forma_pago, plazo_dias, "
            + "numero_factura, subtotal, igv, total, id_usuario";

    @Override
    public void insertar(Compra compra) {
        String sql = "INSERT INTO " + TABLA
                + " (id_orden_compra, id_proveedor, fecha, id_forma_pago, plazo_dias, numero_factura, "
                + "subtotal, igv, total, id_usuario) VALUES (?, ?, now(), ?, ?, ?, ?, ?, ?, ?) "
                + "RETURNING id_compra, fecha";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (compra.getIdOrdenCompra() != null) {
                ps.setInt(1, compra.getIdOrdenCompra());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, compra.getIdProveedor());
            ps.setInt(3, compra.getIdFormaPago());
            if (compra.getPlazoDias() != null) {
                ps.setInt(4, compra.getPlazoDias());
            } else {
                ps.setNull(4, Types.SMALLINT);
            }
            ps.setString(5, compra.getNumeroFactura());
            ps.setBigDecimal(6, compra.getSubtotal());
            ps.setBigDecimal(7, compra.getIgv());
            ps.setBigDecimal(8, compra.getTotal());
            ps.setInt(9, compra.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la compra");
                }
                compra.setIdCompra(rs.getInt("id_compra"));
                compra.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar compra", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Compra buscarPorId(Integer idCompra) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_compra = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar compra " + idCompra, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Compra buscarPorNumeroFactura(int idProveedor, String numeroFactura) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA
                + " WHERE id_proveedor = ? AND numero_factura = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProveedor);
            ps.setString(2, numeroFactura);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar compra por número de factura", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Compra buscarPorOrdenCompra(int idOrdenCompra) {

        String sql = "SELECT " + COLUMNAS
                + " FROM " + TABLA
                + " WHERE id_orden_compra = ?";

        Connection cn = obtenerConexion();

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idOrdenCompra);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }

        } catch (SQLException e) {
            throw error(
                    "Error al buscar compra por orden de compra",
                    e
            );
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Compra> listar() {
        Connection cn = obtenerConexion();
        List<Compra> resultado = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha DESC";
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar compras", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<CompraConsulta> consultarHistorial(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer idProveedor,
            Integer idFormaPago,
            Boolean conOrdenCompra) {

        StringBuilder sql = new StringBuilder(
                "SELECT "
                + "c.id_compra, "
                + "c.id_orden_compra, "
                + "c.fecha, "
                + "c.id_proveedor, "
                + "p.razon_social, "
                + "p.ruc, "
                + "c.id_forma_pago, "
                + "fp.nombre AS nombre_forma_pago, "
                + "c.plazo_dias, "
                + "c.numero_factura, "
                + "c.subtotal, "
                + "c.igv, "
                + "c.total, "
                + "c.id_usuario, "
                + "u.nombres || ' ' || u.apellidos AS nombre_usuario "
                + "FROM compra c "
                + "JOIN proveedor p "
                + "ON p.id_proveedor = c.id_proveedor "
                + "JOIN forma_pago fp "
                + "ON fp.id_forma_pago = c.id_forma_pago "
                + "JOIN usuario u "
                + "ON u.id_usuario = c.id_usuario "
                + "WHERE 1 = 1 "
        );

        if (fechaDesde != null) {
            sql.append("AND c.fecha >= ? ");
        }

        if (fechaHasta != null) {
            sql.append("AND c.fecha < ? ");
        }

        if (idProveedor != null) {
            sql.append("AND c.id_proveedor = ? ");
        }

        if (idFormaPago != null) {
            sql.append("AND c.id_forma_pago = ? ");
        }

        if (conOrdenCompra != null) {

            if (conOrdenCompra) {
                sql.append("AND c.id_orden_compra IS NOT NULL ");
            } else {
                sql.append("AND c.id_orden_compra IS NULL ");
            }
        }

        sql.append(
                "ORDER BY c.fecha DESC, c.id_compra DESC"
        );

        Connection cn = obtenerConexion();

        List<CompraConsulta> resultado
                = new ArrayList<>();

        try (PreparedStatement ps
                = cn.prepareStatement(sql.toString())) {

            int i = 1;

            if (fechaDesde != null) {

                ps.setTimestamp(
                        i++,
                        Timestamp.valueOf(
                                fechaDesde.atStartOfDay()
                        )
                );
            }

            if (fechaHasta != null) {

                ps.setTimestamp(
                        i++,
                        Timestamp.valueOf(
                                fechaHasta
                                        .plusDays(1)
                                        .atStartOfDay()
                        )
                );
            }

            if (idProveedor != null) {
                ps.setInt(
                        i++,
                        idProveedor
                );
            }

            if (idFormaPago != null) {
                ps.setInt(
                        i++,
                        idFormaPago
                );
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    resultado.add(
                            mapearConsulta(rs)
                    );
                }
            }

            return resultado;

        } catch (SQLException e) {

            throw error(
                    "Error al consultar historial de compras",
                    e
            );

        } finally {

            cerrar(cn);
        }
    }

    private Compra mapear(ResultSet rs) throws SQLException {
        Compra compra = new Compra();
        compra.setIdCompra(rs.getInt("id_compra"));

        int idOrdenCompra = rs.getInt("id_orden_compra");
        compra.setIdOrdenCompra(rs.wasNull() ? null : idOrdenCompra);

        compra.setIdProveedor(rs.getInt("id_proveedor"));
        compra.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        compra.setIdFormaPago(rs.getInt("id_forma_pago"));

        int plazoDias = rs.getInt("plazo_dias");
        compra.setPlazoDias(rs.wasNull() ? null : plazoDias);

        compra.setNumeroFactura(rs.getString("numero_factura"));
        compra.setSubtotal(rs.getBigDecimal("subtotal"));
        compra.setIgv(rs.getBigDecimal("igv"));
        compra.setTotal(rs.getBigDecimal("total"));
        compra.setIdUsuario(rs.getInt("id_usuario"));
        return compra;
    }

    private CompraConsulta mapearConsulta(
            ResultSet rs) throws SQLException {

        int idOrdenCompra
                = rs.getInt("id_orden_compra");

        Integer ordenCompra
                = rs.wasNull()
                ? null
                : idOrdenCompra;

        int plazoDias
                = rs.getInt("plazo_dias");

        Integer plazo
                = rs.wasNull()
                ? null
                : plazoDias;

        return new CompraConsulta(
                rs.getInt("id_compra"),
                ordenCompra,
                rs.getTimestamp("fecha")
                        .toLocalDateTime(),
                rs.getInt("id_proveedor"),
                rs.getString("razon_social"),
                rs.getString("ruc"),
                rs.getInt("id_forma_pago"),
                rs.getString("nombre_forma_pago"),
                plazo,
                rs.getString("numero_factura"),
                rs.getBigDecimal("subtotal"),
                rs.getBigDecimal("igv"),
                rs.getBigDecimal("total"),
                rs.getInt("id_usuario"),
                rs.getString("nombre_usuario")
        );
    }
}
