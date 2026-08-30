package com.ferronor.sic.tesoreria.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.tesoreria.modelo.CuentaBancaria;
import com.ferronor.sic.tesoreria.modelo.Moneda;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CuentaBancariaDAOImpl extends AbstractDAO implements CuentaBancariaDAO {

    private static final String TABLA = "cuenta_bancaria";
    private static final String COLUMNAS
            = "id_cuenta_bancaria, banco, alias, numero_cuenta, cci, moneda, saldo_actual, activa";

    @Override
    public void insertar(CuentaBancaria cb) {
        String sql = "INSERT INTO " + TABLA
                + " (banco, alias, numero_cuenta, cci, moneda, saldo_actual, activa) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_cuenta_bancaria";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cb.getBanco());
            ps.setString(2, cb.getAlias());
            ps.setString(3, cb.getNumeroCuenta());
            ps.setString(4, cb.getCci());
            ps.setString(5, cb.getMoneda().name());
            ps.setBigDecimal(6, cb.getSaldoActual());
            ps.setBoolean(7, cb.isActiva());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la cuenta bancaria");
                }
                cb.setIdCuentaBancaria(rs.getInt("id_cuenta_bancaria"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar cuenta bancaria", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(CuentaBancaria cb) {
        String sql = "UPDATE " + TABLA + " SET banco = ?, alias = ?, cci = ?, saldo_actual = ?, activa = ? "
                + "WHERE id_cuenta_bancaria = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cb.getBanco());
            ps.setString(2, cb.getAlias());
            ps.setString(3, cb.getCci());
            ps.setBigDecimal(4, cb.getSaldoActual());
            ps.setBoolean(5, cb.isActiva());
            ps.setInt(6, cb.getIdCuentaBancaria());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe cuenta bancaria con id " + cb.getIdCuentaBancaria());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar cuenta bancaria " + cb.getIdCuentaBancaria(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizarSaldo(int idCuentaBancaria, BigDecimal nuevoSaldo) {
        String sql = "UPDATE " + TABLA + " SET saldo_actual = ? WHERE id_cuenta_bancaria = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoSaldo);
            ps.setInt(2, idCuentaBancaria);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe cuenta bancaria con id " + idCuentaBancaria);
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar el saldo de la cuenta bancaria " + idCuentaBancaria, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public CuentaBancaria buscarPorId(Integer idCuentaBancaria) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_cuenta_bancaria = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCuentaBancaria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar cuenta bancaria " + idCuentaBancaria, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<CuentaBancaria> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY banco");
    }

    @Override
    public List<CuentaBancaria> listarActivas() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE activa = TRUE ORDER BY banco");
    }

    private List<CuentaBancaria> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<CuentaBancaria> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar cuentas bancarias", e);
        } finally {
            cerrar(cn);
        }
    }

    private CuentaBancaria mapear(ResultSet rs) throws SQLException {
        CuentaBancaria cb = new CuentaBancaria();
        cb.setIdCuentaBancaria(rs.getInt("id_cuenta_bancaria"));
        cb.setBanco(rs.getString("banco"));
        cb.setAlias(rs.getString("alias"));
        cb.setNumeroCuenta(rs.getString("numero_cuenta"));
        cb.setCci(rs.getString("cci"));
        cb.setMoneda(Moneda.valueOf(rs.getString("moneda")));
        cb.setSaldoActual(rs.getBigDecimal("saldo_actual"));
        cb.setActiva(rs.getBoolean("activa"));
        return cb;
    }
}