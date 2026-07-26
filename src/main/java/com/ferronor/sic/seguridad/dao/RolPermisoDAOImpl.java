/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.dao;

import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RolPermisoDAOImpl extends AbstractDAO implements RolPermisoDAO {

    @Override
    public void asignar(int idRol, int idPermiso) {
        String sql = "INSERT INTO rol_permiso (id_rol, id_permiso) VALUES (?, ?) ON CONFLICT DO NOTHING";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            ps.setInt(2, idPermiso);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("Error al asignar permiso al rol", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void revocar(int idRol, int idPermiso) {
        String sql = "DELETE FROM rol_permiso WHERE id_rol = ? AND id_permiso = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            ps.setInt(2, idPermiso);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("Error al revocar permiso del rol", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Integer> listarIdsPermisoPorRol(int idRol) {
        String sql = "SELECT id_permiso FROM rol_permiso WHERE id_rol = ?";
        Connection cn = obtenerConexion();
        List<Integer> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(rs.getInt("id_permiso"));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar permisos del rol " + idRol, e);
        } finally {
            cerrar(cn);
        }
    }
}
