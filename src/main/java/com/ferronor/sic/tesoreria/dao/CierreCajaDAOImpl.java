package com.ferronor.sic.tesoreria.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.tesoreria.modelo.CierreCaja;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CierreCajaDAOImpl extends AbstractDAO implements CierreCajaDAO {

    private static final String TABLA = "cierre_caja";
    private static final String COLUMNAS
            = "id_cierre, id_caja, fecha, saldo_inicial, saldo_final_sistema, saldo_final_real, "
            + "diferencia, id_usuario";

    @Override
    public void insertar(CierreCaja cierre) {
        String sql = "INSERT INTO " + TABLA
                + " (id_caja, fecha, saldo_inicial, saldo_final_sistema, saldo_final_real, diferencia, id_usuario) "
                + "VALUES (?, now(), ?, ?, ?, ?, ?) RETURNING id_cierre, fecha";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cierre.getIdCaja());
            ps.setBigDecimal(2, cierre.getSaldoInicial());
            ps.setBigDecimal(3, cierre.getSaldoFinalSistema());
            ps.setBigDecimal(4, cierre.getSaldoFinalReal());
            ps.setBigDecimal(5, cierre.getDiferencia());
            ps.setInt(6, cierre.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el cierre de caja");
                }
                cierre.setIdCierre(rs.getInt("id_cierre"));
                cierre.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar cierre de caja", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public CierreCaja buscarPorId(Integer idCierre) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_cierre = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCierre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar cierre de caja " + idCierre, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<CierreCaja> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha DESC");
    }

    @Override
    public List<CierreCaja> listarPorCaja(int idCaja) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_caja = ? ORDER BY fecha DESC";
        List<CierreCaja> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar cierres de la caja " + idCaja, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<CierreCaja> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<CierreCaja> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar cierres de caja", e);
        } finally {
            cerrar(cn);
        }
    }

    private CierreCaja mapear(ResultSet rs) throws SQLException {
        CierreCaja cierre = new CierreCaja();
        cierre.setIdCierre(rs.getInt("id_cierre"));
        cierre.setIdCaja(rs.getInt("id_caja"));
        cierre.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        cierre.setSaldoInicial(rs.getBigDecimal("saldo_inicial"));
        cierre.setSaldoFinalSistema(rs.getBigDecimal("saldo_final_sistema"));
        cierre.setSaldoFinalReal(rs.getBigDecimal("saldo_final_real"));
        cierre.setDiferencia(rs.getBigDecimal("diferencia"));
        cierre.setIdUsuario(rs.getInt("id_usuario"));
        return cierre;
    }
}