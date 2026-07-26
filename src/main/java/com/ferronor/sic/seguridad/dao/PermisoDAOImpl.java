/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.seguridad.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.seguridad.modelo.Permiso;

import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;

/**
 *
 * @author JEFERSON
 */
public class PermisoDAOImpl extends AbstractDAO implements PermisoDAO {

    private static final String TABLA = "permiso";
    private static final String COLUMNAS = "id_permiso,codigo,nombre";

    // PermisoDAOImpl — implementación de listarPorRol()
    @Override
    public List<Permiso> listarPorRol(int idRol) {
        String sql = "SELECT p.id_permiso, p.codigo, p.nombre FROM permiso p "
                + "JOIN rol_permiso rp ON rp.id_permiso = p.id_permiso "
                + "WHERE rp.id_rol = ? ORDER BY p.nombre";
        Connection cn = obtenerConexion();
        List<Permiso> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar permisos del rol " + idRol, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void insertar(Permiso entidad) {
        String sql = "INSERT INTO " + TABLA + " (codigo, nombre) VALUES (?,?) RETURNING id_permiso";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, entidad.getCodigo());
            ps.setString(2, entidad.getNombre());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el permiso");
                }
                entidad.setIdPermiso(rs.getInt("id_permiso"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar permiso", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(Permiso entidad) {
        String sql = "UPDATE " + TABLA + " SET codigo=?, nombre=? WHERE id_permiso = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, entidad.getCodigo());
            ps.setString(2, entidad.getNombre());
            ps.setInt(3, entidad.getIdPermiso());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe permiso con id " + entidad.getIdPermiso());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar permiso " + entidad.getIdPermiso(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Permiso buscarPorId(Integer id) {

        return buscarPorCampo("id_permiso = ?", id);
    }

    @Override
    public Permiso buscarPorCodigo(String codigo) {

        return buscarPorCampo("UPPER(codigo)=UPPER(?)", codigo);

    }

    private Permiso buscarPorCampo(String condicion, Object valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar permiso", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Permiso> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY nombre";
        Connection cn = obtenerConexion();
        List<Permiso> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar permisos", e);
        } finally {
            cerrar(cn);
        }
    }

    private Permiso mapear(ResultSet rs) throws SQLException {
        Permiso p = new Permiso(rs.getString("codigo"), rs.getString("nombre"));
        p.setIdPermiso(rs.getInt("id_permiso"));
        return p;
    }

}
