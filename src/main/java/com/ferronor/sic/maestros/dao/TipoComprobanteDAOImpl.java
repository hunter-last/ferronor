/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.maestros.modelo.TipoComprobante;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoComprobanteDAOImpl extends AbstractDAO implements TipoComprobanteDAO {

    private static final String TABLA = "tipo_comprobante";
    private static final String COLUMNAS = "id_tipo_comprobante, nombre, serie";

    @Override
    public void insertar(TipoComprobante t) {
        String sql = "INSERT INTO " + TABLA + " (nombre, serie) VALUES (?, ?) RETURNING id_tipo_comprobante";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getSerie());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el tipo de comprobante");
                }
                t.setIdTipoComprobante(rs.getInt("id_tipo_comprobante"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar tipo de comprobante", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(TipoComprobante t) {
        String sql = "UPDATE " + TABLA + " SET nombre = ?, serie = ? WHERE id_tipo_comprobante = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getSerie());
            ps.setInt(3, t.getIdTipoComprobante());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe tipo de comprobante con id " + t.getIdTipoComprobante());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar tipo de comprobante " + t.getIdTipoComprobante(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public TipoComprobante buscarPorId(Integer id) {
        return buscarPorCampo("id_tipo_comprobante = ?", id);
    }

    @Override
    public TipoComprobante buscarPorNombre(String nombre) {
        return buscarPorCampo("UPPER(nombre) = UPPER(?)", nombre);
    }

    private TipoComprobante buscarPorCampo(String condicion, Object valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar tipo de comprobante", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<TipoComprobante> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY nombre";
        Connection cn = obtenerConexion();
        List<TipoComprobante> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar tipos de comprobante", e);
        } finally {
            cerrar(cn);
        }
    }

    private TipoComprobante mapear(ResultSet rs) throws SQLException {
        TipoComprobante t = new TipoComprobante(rs.getString("nombre"), rs.getString("serie"));
        t.setIdTipoComprobante(rs.getInt("id_tipo_comprobante"));
        return t;
    }
}
