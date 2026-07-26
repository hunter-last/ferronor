/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.inventario.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.inventario.modelo.AjusteInventario;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AjusteInventarioDAOImpl extends AbstractDAO implements AjusteInventarioDAO {

    private static final String TABLA = "ajuste_inventario";

    private static final String COLUMNAS
            = "id_ajuste, id_producto, fecha, cantidad_sistema, cantidad_fisica, diferencia, motivo, id_usuario, id_movimiento_generado";

    @Override
    public void insertar(AjusteInventario ajuste) {
        String sql = "INSERT INTO " + TABLA
                + " (id_producto, fecha, cantidad_sistema, cantidad_fisica, diferencia, motivo, id_usuario, id_movimiento_generado) "
                + "VALUES (?, now(), ?, ?, ?, ?, ?, ?)";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ajuste.getIdProducto());
            ps.setBigDecimal(2, ajuste.getCantidadSistema());
            ps.setBigDecimal(3, ajuste.getCantidadFisica());
            ps.setBigDecimal(4, ajuste.getDiferencia());
            ps.setString(5, ajuste.getMotivo());
            ps.setInt(6, ajuste.getIdUsuario());
            ps.setInt(7, ajuste.getIdMovimientoGenerado());
            ps.executeUpdate();
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el ajuste del producto " + ajuste.getIdProducto());
                }
                ajuste.setIdAjuste(rs.getInt("id_ajuste"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar ajuste de inventario del producto " + ajuste.getIdProducto(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public AjusteInventario buscarPorId(Integer idAjuste) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + "WHERE id_ajuste = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAjuste);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar ajuste de inventario " + idAjuste, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<AjusteInventario> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha ASC, id_ajuste ASC";
        Connection cn = obtenerConexion();
        List<AjusteInventario> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar ajustes de inventario", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<AjusteInventario> listarPorProducto(int idProducto) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_producto = ? ORDER BY fecha ASC, id_ajuste ASC";
        List<AjusteInventario> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar ajustes del producto " + idProducto, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<AjusteInventario> listarPorRangoFecha(LocalDate desde, LocalDate hasta) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA
                + " WHERE fecha >= ? AND fecha < ? ORDER BY fecha ASC, id_ajuste ASC";
        List<AjusteInventario> resultado = new ArrayList<>();
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
            throw error("Error al listar ajustes por rango de fecha", e);
        } finally {
            cerrar(cn);
        }
    }

    private AjusteInventario mapear(ResultSet rs) throws SQLException {
        return AjusteInventario.reconstruir(
                rs.getInt("id_ajuste"), rs.getInt("id_producto"),
                rs.getTimestamp("fecha").toLocalDateTime(),
                rs.getBigDecimal("cantidad_sistema"), rs.getBigDecimal("cantidad_fisica"),
                rs.getBigDecimal("diferencia"), rs.getString("motivo"),
                rs.getInt("id_usuario"), rs.getInt("id_movimiento_generado")
        );
    }

}
