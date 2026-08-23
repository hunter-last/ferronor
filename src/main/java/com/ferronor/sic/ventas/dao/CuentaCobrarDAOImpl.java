package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.ventas.modelo.CuentaCobrar;
import com.ferronor.sic.ventas.modelo.EstadoCuenta;
import com.ferronor.sic.ventas.modelo.dto.CuentaCobrarConsulta;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CuentaCobrarDAOImpl extends AbstractDAO implements CuentaCobrarDAO {

    private static final String TABLA = "cuenta_cobrar";
    private static final String COLUMNAS
            = "id_cuenta_cobrar, id_venta, monto_total, monto_cobrado, saldo_pendiente, "
            + "fecha_vencimiento, estado";

    @Override
    public void insertar(CuentaCobrar cc) {
        String sql = "INSERT INTO " + TABLA
                + " (id_venta, monto_total, monto_cobrado, saldo_pendiente, fecha_vencimiento, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_cuenta_cobrar";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cc.getIdVenta());
            ps.setBigDecimal(2, cc.getMontoTotal());
            ps.setBigDecimal(3, cc.getMontoCobrado());
            ps.setBigDecimal(4, cc.getSaldoPendiente());
            if (cc.getFechaVencimiento() != null) {
                ps.setDate(5, Date.valueOf(cc.getFechaVencimiento()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, cc.getEstado().name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la cuenta por cobrar de la venta " + cc.getIdVenta());
                }
                cc.setIdCuentaCobrar(rs.getInt("id_cuenta_cobrar"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar cuenta por cobrar de la venta " + cc.getIdVenta(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(CuentaCobrar cc) {
        String sql = "UPDATE " + TABLA
                + " SET monto_total = ?, monto_cobrado = ?, saldo_pendiente = ?, fecha_vencimiento = ?, estado = ? "
                + "WHERE id_cuenta_cobrar = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, cc.getMontoTotal());
            ps.setBigDecimal(2, cc.getMontoCobrado());
            ps.setBigDecimal(3, cc.getSaldoPendiente());
            if (cc.getFechaVencimiento() != null) {
                ps.setDate(4, Date.valueOf(cc.getFechaVencimiento()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, cc.getEstado().name());
            ps.setInt(6, cc.getIdCuentaCobrar());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe cuenta por cobrar con id " + cc.getIdCuentaCobrar());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar cuenta por cobrar " + cc.getIdCuentaCobrar(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void registrarCobro(int idCuentaCobrar, BigDecimal nuevoMontoCobrado, BigDecimal nuevoSaldoPendiente,
            EstadoCuenta nuevoEstado) {
        String sql = "UPDATE " + TABLA + " SET monto_cobrado = ?, saldo_pendiente = ?, estado = ? "
                + "WHERE id_cuenta_cobrar = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoMontoCobrado);
            ps.setBigDecimal(2, nuevoSaldoPendiente);
            ps.setString(3, nuevoEstado.name());
            ps.setInt(4, idCuentaCobrar);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe cuenta por cobrar con id " + idCuentaCobrar);
                }
            }
        } catch (SQLException e) {
            throw error("Error al registrar cobro de la cuenta por cobrar " + idCuentaCobrar, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public CuentaCobrar buscarPorId(Integer idCuentaCobrar) {
        return buscarPorCampo("id_cuenta_cobrar = ?", idCuentaCobrar);
    }

    @Override
    public CuentaCobrar buscarPorVenta(int idVenta) {
        return buscarPorCampo("id_venta = ?", idVenta);
    }

    private CuentaCobrar buscarPorCampo(String condicion, int valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar cuenta por cobrar", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<CuentaCobrar> listar() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha_vencimiento");
    }

    @Override
    public List<CuentaCobrar> listarPorEstado(EstadoCuenta estado) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE estado = ? ORDER BY fecha_vencimiento";
        List<CuentaCobrar> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar cuentas por cobrar por estado " + estado, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<CuentaCobrarConsulta> consultar(EstadoCuenta estado, Integer idCliente,
            LocalDate fechaDesde, LocalDate fechaHasta) {

        StringBuilder sql = new StringBuilder(
                "SELECT "
                + "cc.id_cuenta_cobrar, "
                + "cc.id_venta, "
                + "cl.nombre_razon_social, "
                + "cl.tipo_documento, "
                + "cl.numero_documento, "
                + "v.fecha AS fecha_venta, "
                + "cc.fecha_vencimiento, "
                + "cc.monto_total, "
                + "cc.monto_cobrado, "
                + "cc.saldo_pendiente, "
                + "cc.estado "
                + "FROM cuenta_cobrar cc "
                + "JOIN venta v ON v.id_venta = cc.id_venta "
                + "JOIN cliente cl ON cl.id_cliente = v.id_cliente "
                + "WHERE 1 = 1"
        );

        if (estado != null) {
            sql.append(" AND cc.estado = ?");
        }

        if (idCliente != null) {
            sql.append(" AND v.id_cliente = ?");
        }

        if (fechaDesde != null) {
            sql.append(" AND cc.fecha_vencimiento >= ?");
        }

        if (fechaHasta != null) {
            sql.append(" AND cc.fecha_vencimiento < ?");
        }

        sql.append(" ORDER BY cc.fecha_vencimiento ASC NULLS LAST");

        Connection cn = obtenerConexion();
        List<CuentaCobrarConsulta> resultado = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            int i = 1;

            if (estado != null) {
                ps.setString(i++, estado.name());
            }

            if (idCliente != null) {
                ps.setInt(i++, idCliente);
            }

            if (fechaDesde != null) {
                ps.setDate(i++, Date.valueOf(fechaDesde));
            }

            if (fechaHasta != null) {
                ps.setDate(i++, Date.valueOf(fechaHasta.plusDays(1)));
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    resultado.add(mapearConsulta(rs));
                }
            }

            return resultado;

        } catch (SQLException e) {
            throw error("Error al consultar cuentas por cobrar", e);
        } finally {
            cerrar(cn);
        }
    }

    private CuentaCobrarConsulta mapearConsulta(ResultSet rs)
            throws SQLException {

        Date fechaVencimiento = rs.getDate("fecha_vencimiento");

        return new CuentaCobrarConsulta(
                rs.getInt("id_cuenta_cobrar"),
                rs.getInt("id_venta"),
                rs.getString("nombre_razon_social"),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento"),
                rs.getTimestamp("fecha_venta").toLocalDateTime(),
                fechaVencimiento != null
                        ? fechaVencimiento.toLocalDate()
                        : null,
                rs.getBigDecimal("monto_total"),
                rs.getBigDecimal("monto_cobrado"),
                rs.getBigDecimal("saldo_pendiente"),
                EstadoCuenta.valueOf(rs.getString("estado"))
        );
    }

    private List<CuentaCobrar> listarConFiltro(String sql) {
        Connection cn = obtenerConexion();
        List<CuentaCobrar> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar cuentas por cobrar", e);
        } finally {
            cerrar(cn);
        }
    }

    private CuentaCobrar mapear(ResultSet rs) throws SQLException {
        CuentaCobrar cc = new CuentaCobrar();
        cc.setIdCuentaCobrar(rs.getInt("id_cuenta_cobrar"));
        cc.setIdVenta(rs.getInt("id_venta"));
        cc.setMontoTotal(rs.getBigDecimal("monto_total"));
        cc.setMontoCobrado(rs.getBigDecimal("monto_cobrado"));
        cc.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente"));
        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        cc.setFechaVencimiento(fechaVencimiento != null ? fechaVencimiento.toLocalDate() : null);
        cc.setEstado(EstadoCuenta.valueOf(rs.getString("estado")));
        return cc;
    }
}
