package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.CuentaPagar;
import com.ferronor.sic.compras.modelo.EstadoCuenta;
import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CuentaPagarDAOImpl extends AbstractDAO implements CuentaPagarDAO {

    private static final String TABLA = "cuenta_pagar";
    private static final String COLUMNAS
            = "id_cuenta_pagar, id_compra, monto_total, monto_pagado, saldo_pendiente, "
            + "fecha_vencimiento, estado";

    @Override
    public void insertar(CuentaPagar cp) {
        String sql = "INSERT INTO " + TABLA
                + " (id_compra, monto_total, monto_pagado, saldo_pendiente, fecha_vencimiento, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_cuenta_pagar";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cp.getIdCompra());
            ps.setBigDecimal(2, cp.getMontoTotal());
            ps.setBigDecimal(3, cp.getMontoPagado());
            ps.setBigDecimal(4, cp.getSaldoPendiente());
            ps.setDate(5, java.sql.Date.valueOf(cp.getFechaVencimiento()));
            ps.setString(6, cp.getEstado().name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la cuenta por pagar de la compra " + cp.getIdCompra());
                }
                cp.setIdCuentaPagar(rs.getInt("id_cuenta_pagar"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar cuenta por pagar de la compra " + cp.getIdCompra(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(CuentaPagar cp) {
        String sql = "UPDATE " + TABLA
                + " SET monto_total = ?, monto_pagado = ?, saldo_pendiente = ?, fecha_vencimiento = ?, estado = ? "
                + "WHERE id_cuenta_pagar = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, cp.getMontoTotal());
            ps.setBigDecimal(2, cp.getMontoPagado());
            ps.setBigDecimal(3, cp.getSaldoPendiente());
            ps.setDate(4, java.sql.Date.valueOf(cp.getFechaVencimiento()));
            ps.setString(5, cp.getEstado().name());
            ps.setInt(6, cp.getIdCuentaPagar());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe cuenta por pagar con id " + cp.getIdCuentaPagar());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar cuenta por pagar " + cp.getIdCuentaPagar(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void registrarPago(int idCuentaPagar, BigDecimal nuevoMontoPagado,
            BigDecimal nuevoSaldoPendiente, EstadoCuenta nuevoEstado) {
        String sql = "UPDATE " + TABLA + " SET monto_pagado = ?, saldo_pendiente = ?, estado = ? "
                + "WHERE id_cuenta_pagar = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoMontoPagado);
            ps.setBigDecimal(2, nuevoSaldoPendiente);
            ps.setString(3, nuevoEstado.name());
            ps.setInt(4, idCuentaPagar);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe cuenta por pagar con id " + idCuentaPagar);
                }
            }
        } catch (SQLException e) {
            throw error("Error al registrar pago de la cuenta por pagar " + idCuentaPagar, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public CuentaPagar buscarPorId(Integer idCuentaPagar) {
        return buscarPorCampo("id_cuenta_pagar = ?", idCuentaPagar);
    }

    @Override
    public CuentaPagar buscarPorCompra(int idCompra) {
        return buscarPorCampo("id_compra = ?", idCompra);
    }

    private CuentaPagar buscarPorCampo(String condicion, int valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar cuenta por pagar", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<CuentaPagar> listar() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha_vencimiento");
    }

    @Override
    public List<CuentaPagar> listarPorEstado(EstadoCuenta estado) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE estado = ? ORDER BY fecha_vencimiento";
        List<CuentaPagar> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar cuentas por pagar por estado " + estado, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<CuentaPagar> listarConFiltro(String sql) {
        Connection cn = obtenerConexion();
        List<CuentaPagar> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar cuentas por pagar", e);
        } finally {
            cerrar(cn);
        }
    }

    private CuentaPagar mapear(ResultSet rs) throws SQLException {
        CuentaPagar cp = new CuentaPagar();
        cp.setIdCuentaPagar(rs.getInt("id_cuenta_pagar"));
        cp.setIdCompra(rs.getInt("id_compra"));
        cp.setMontoTotal(rs.getBigDecimal("monto_total"));
        cp.setMontoPagado(rs.getBigDecimal("monto_pagado"));
        cp.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente"));
        cp.setFechaVencimiento(rs.getDate("fecha_vencimiento").toLocalDate());
        cp.setEstado(EstadoCuenta.valueOf(rs.getString("estado")));
        return cp;
    }
}