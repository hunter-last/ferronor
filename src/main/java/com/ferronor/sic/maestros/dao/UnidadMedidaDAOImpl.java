/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.maestros.modelo.UnidadMedida;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UnidadMedidaDAOImpl extends AbstractDAO implements UnidadMedidaDAO {

    private static final String TABLA = "unidad_medida";
    private static final String COLUMNAS = "id_unidad_medida, nombre, abreviatura";

    @Override
    public void insertar(UnidadMedida u) {
        String sql = "INSERT INTO " + TABLA + " (nombre, abreviatura) VALUES (?, ?) RETURNING id_unidad_medida";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getAbreviatura());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la unidad de medida");
                }
                u.setIdUnidadMedida(rs.getInt("id_unidad_medida"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar unidad de medida", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(UnidadMedida u) {
        String sql = "UPDATE " + TABLA + " SET nombre = ?, abreviatura = ? WHERE id_unidad_medida = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getAbreviatura());
            ps.setInt(3, u.getIdUnidadMedida());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe unidad de medida con id " + u.getIdUnidadMedida());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar unidad de medida " + u.getIdUnidadMedida(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public UnidadMedida buscarPorId(Integer id) {
        return buscarPorCampo("id_unidad_medida = ?", id);
    }

    @Override
    public UnidadMedida buscarPorNombre(String nombre) {
        return buscarPorCampo("UPPER(nombre) = UPPER(?)", nombre);
    }

    @Override
    public UnidadMedida buscarPorAbreviatura(String abreviatura) {
        return buscarPorCampo("UPPER(abreviatura) = UPPER(?)", abreviatura);
    }

    private UnidadMedida buscarPorCampo(String condicion, Object valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar unidad de medida", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<UnidadMedida> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY nombre";
        Connection cn = obtenerConexion();
        List<UnidadMedida> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar unidades de medida", e);
        } finally {
            cerrar(cn);
        }
    }

    private UnidadMedida mapear(ResultSet rs) throws SQLException {
        UnidadMedida u = new UnidadMedida(rs.getString("nombre"), rs.getString("abreviatura"));
        u.setIdUnidadMedida(rs.getInt("id_unidad_medida"));
        return u;
    }

}
