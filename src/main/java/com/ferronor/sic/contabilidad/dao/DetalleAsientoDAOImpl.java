/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.dao;

import com.ferronor.sic.contabilidad.modelo.DetalleAsiento;
import com.ferronor.sic.contabilidad.modelo.dto.MovimientoCuenta;
import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class DetalleAsientoDAOImpl extends AbstractDAO implements DetalleAsientoDAO {

    private static final String TABLA = "detalle_asiento";
    private static final String COLUMNAS = "id_detalle, id_asiento, id_cuenta, debe, haber";

    @Override
    public void insertar(DetalleAsiento d) {
        String sql = "INSERT INTO " + TABLA + " (id_asiento, id_cuenta, debe, haber) VALUES (?, ?, ?, ?) RETURNING id_detalle";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, d.getIdAsiento());
            ps.setInt(2, d.getIdCuenta());
            ps.setBigDecimal(3, d.getDebe());
            ps.setBigDecimal(4, d.getHaber());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new DaoException("No se pudo insertar el detalle del asiento");
                d.setIdDetalle(rs.getInt("id_detalle"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar detalle de asiento", e);
        } finally { cerrar(cn); }
    }

    @Override
    public List<DetalleAsiento> listarPorAsiento(int idAsiento) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_asiento = ? ORDER BY id_detalle";
        Connection cn = obtenerConexion();
        List<DetalleAsiento> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAsiento);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) resultado.add(mapear(rs)); }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar detalle del asiento " + idAsiento, e);
        } finally { cerrar(cn); }
    }
    
    
    @Override
    public List<DetalleAsiento> listarPorAsientos(List<Integer> idsAsiento) {
        if (idsAsiento.isEmpty()) return new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_asiento = ANY(?) ORDER BY id_asiento, id_detalle";
        Connection cn = obtenerConexion();
        List<DetalleAsiento> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setArray(1, cn.createArrayOf("integer", idsAsiento.toArray(new Integer[0])));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) resultado.add(mapear(rs)); }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar detalle de asientos", e);
        } finally { cerrar(cn); }
    }

    @Override
    public List<MovimientoCuenta> listarMovimientosPorCuenta(int idCuenta, LocalDate hasta) {
        String sql = "SELECT a.fecha, a.glosa, da.debe, da.haber " +
                     "FROM " + TABLA + " da JOIN asiento_contable a ON da.id_asiento = a.id_asiento " +
                     "WHERE da.id_cuenta = ? AND a.estado = 'ACTIVO' AND a.fecha < ? " +
                     "ORDER BY a.fecha ASC, da.id_detalle ASC";
        Connection cn = obtenerConexion();
        List<MovimientoCuenta> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCuenta);
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(new MovimientoCuenta(
                    rs.getTimestamp("fecha").toLocalDateTime(), rs.getString("glosa"),
                    rs.getBigDecimal("debe"), rs.getBigDecimal("haber")));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar movimientos de la cuenta " + idCuenta, e);
        } finally { cerrar(cn); }
    }


    private DetalleAsiento mapear(ResultSet rs) throws SQLException {
        DetalleAsiento d = rs.getBigDecimal("debe").compareTo(java.math.BigDecimal.ZERO) > 0
            ? DetalleAsiento.debe(rs.getInt("id_cuenta"), rs.getBigDecimal("debe"))
            : DetalleAsiento.haber(rs.getInt("id_cuenta"), rs.getBigDecimal("haber"));
        d.setIdDetalle(rs.getInt("id_detalle"));
        d.setIdAsiento(rs.getInt("id_asiento"));
        return d;
    }
}