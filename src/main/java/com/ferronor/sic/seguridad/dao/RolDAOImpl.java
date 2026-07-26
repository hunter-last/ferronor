/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.seguridad.modelo.Rol;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RolDAOImpl extends AbstractDAO implements RolDAO {

    private static final String TABLA = "rol";
    private static final String COLUMNAS = "id_rol, nombre";

    @Override
    public void insertar(Rol entidad) {
        String sql = "INSERT INTO " + TABLA + " (nombre) VALUES (?) RETURNING id_rol";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el rol");
                }
                entidad.setIdRol(rs.getInt("id_rol"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar rol", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(Rol entidad) {
        String sql = "UPDATE " + TABLA + " SET nombre=? WHERE id_rol=? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setInt(2, entidad.getIdRol());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe rol con id " + entidad.getIdRol());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar rol " + entidad.getIdRol(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Rol buscarPorNombre(String nombre) {

        return buscarPorCampo("UPPER(nombre)=UPPER(?)", nombre);
    }

    @Override
    public Rol buscarPorId(Integer id) {

        return buscarPorCampo("id_rol = ?", id);
    }

    private Rol buscarPorCampo(String condicion, Object valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearRol(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar rol", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Rol> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY nombre";
        Connection cn = obtenerConexion();
        List<Rol> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapearRol(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar roles", e);
        } finally {
            cerrar(cn);
        }
    }

    private Rol mapearRol(ResultSet rs) throws SQLException {
        Rol r = new Rol(rs.getString("nombre"));
        r.setIdRol(rs.getInt("id_rol"));
        return r;
    }

}
