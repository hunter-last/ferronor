/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.dao;

import com.ferronor.sic.contabilidad.modelo.AsientoContable;
import com.ferronor.sic.contabilidad.modelo.EstadoAsiento;
import com.ferronor.sic.contabilidad.modelo.OrigenAsiento;
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

public class AsientoDAOImpl extends AbstractDAO implements AsientoDAO {

    private static final String TABLA = "asiento_contable";
    private static final String COLUMNAS
            = "id_asiento, fecha, origen, id_documento_origen, glosa, estado, id_usuario";

    @Override
    public void insertar(AsientoContable a) {
        String sql = "INSERT INTO " + TABLA + " (origen, id_documento_origen, glosa, estado, id_usuario) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id_asiento, fecha";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, a.getOrigen().name());
            ps.setInt(2, a.getIdDocumentoOrigen());
            ps.setString(3, a.getGlosa());
            ps.setString(4, a.getEstado().name());
            ps.setInt(5, a.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el asiento contable");
                }
                a.setIdAsiento(rs.getInt("id_asiento"));
                a.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar asiento contable", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void anular(int idAsiento) {
        String sql = "UPDATE " + TABLA + " SET estado = 'ANULADO' WHERE id_asiento = ? AND estado = 'ACTIVO' RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAsiento);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe un asiento ACTIVO con id " + idAsiento);
                }
            }
        } catch (SQLException e) {
            throw error("Error al anular asiento contable " + idAsiento, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public AsientoContable buscarPorId(int idAsiento) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_asiento = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAsiento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar asiento contable " + idAsiento, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<AsientoContable> listarPorRangoFecha(LocalDate desde, LocalDate hasta) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA
                + " WHERE fecha >= ? AND fecha < ? ORDER BY fecha ASC, id_asiento ASC";
        Connection cn = obtenerConexion();
        List<AsientoContable> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar asientos por rango de fecha", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<AsientoContable> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA
                + " WHERE ORDER BY , id_asiento ASC";
        Connection cn = obtenerConexion();
        List<AsientoContable> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar asientos ", e);
        } finally {
            cerrar(cn);
        }
    }

    private AsientoContable mapear(ResultSet rs) throws SQLException {
        AsientoContable a = new AsientoContable(
                OrigenAsiento.valueOf(rs.getString("origen")), rs.getInt("id_documento_origen"),
                rs.getString("glosa"), rs.getInt("id_usuario"));
        a.setIdAsiento(rs.getInt("id_asiento"));
        a.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        a.setEstado(EstadoAsiento.valueOf(rs.getString("estado")));
        return a;
    }
}
