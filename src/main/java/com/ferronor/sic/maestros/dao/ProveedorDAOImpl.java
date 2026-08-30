/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAOImpl extends AbstractDAO implements ProveedorDAO {

    private static final String TABLA = "proveedor";
    private static final String COLUMNAS
            = "id_proveedor, razon_social, ruc, direccion, telefono, contacto, activo";
    private static final int LIMITE_BUSQUEDA = 20;

    @Override
    public void insertar(Proveedor p) {
        String sql = "INSERT INTO " + TABLA
                + " (razon_social, ruc, direccion, telefono, contacto, activo) VALUES (?, ?, ?, ?, ?, ?) RETURNING id_proveedor";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getRazonSocial());
            ps.setString(2, p.getRuc());
            ps.setString(3, p.getDireccion());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getContacto());
            ps.setBoolean(6, p.isActivo());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el proveedor");
                }
                p.setIdProveedor(rs.getInt("id_proveedor"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar proveedor", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(Proveedor p) {
        String sql = "UPDATE " + TABLA + " SET razon_social = ?, ruc = ?, direccion = ?, telefono = ?, contacto = ?, activo = ? "
                + "WHERE id_proveedor = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getRazonSocial());
            ps.setString(2, p.getRuc());
            ps.setString(3, p.getDireccion());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getContacto());
            ps.setBoolean(6, p.isActivo());
            ps.setInt(7, p.getIdProveedor());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe proveedor con id " + p.getIdProveedor());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar proveedor " + p.getIdProveedor(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void activar(int idProveedor) {
        cambiarEstado(idProveedor, true);
    }

    @Override
    public void desactivar(int idProveedor) {
        cambiarEstado(idProveedor, false);
    }
    
        @Override
    public List<Proveedor> buscarActivosPorRazonSocialORucParcial(String texto) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA
                + " WHERE activo = TRUE AND (razon_social ILIKE '%' || ? || '%' "
                + "OR ruc ILIKE '%' || ? || '%') "
                + "ORDER BY razon_social LIMIT ?";
        Connection cn = obtenerConexion();
        List<Proveedor> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            String valor = texto == null ? "" : texto.trim();
            ps.setString(1, valor);
            ps.setString(2, valor);
            ps.setInt(3, LIMITE_BUSQUEDA);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al buscar proveedores por razón social o RUC parcial", e);
        } finally {
            cerrar(cn);
        }
    }

    private void cambiarEstado(int idProveedor, boolean activo) {
        String sql = "UPDATE " + TABLA + " SET activo = ? WHERE id_proveedor = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe proveedor con id " + idProveedor);
                }
            }
        } catch (SQLException e) {
            throw error("Error al cambiar estado del proveedor " + idProveedor, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Proveedor buscarPorId(Integer id) {
        return buscarPorCampo("id_proveedor = ?", id);
    }

    @Override
    public Proveedor buscarPorRuc(String ruc) {
        return buscarPorCampo("ruc = ?", ruc); // el RUC es numérico, sin ambigüedad de mayúsculas/minúsculas
    }

    private Proveedor buscarPorCampo(String condicion, Object valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar proveedor", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Proveedor> listar() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY razon_social");
    }

    @Override
    public List<Proveedor> listarActivos() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE activo = TRUE ORDER BY razon_social");
    }

    private List<Proveedor> listarConFiltro(String sql) {
        Connection cn = obtenerConexion();
        List<Proveedor> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar proveedores", e);
        } finally {
            cerrar(cn);
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor(rs.getString("razon_social"), rs.getString("ruc"),
                rs.getString("direccion"), rs.getString("telefono"), rs.getString("contacto"));
        p.setIdProveedor(rs.getInt("id_proveedor"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }

}
