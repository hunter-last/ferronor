/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.maestros.modelo.Cliente;
import com.ferronor.sic.maestros.modelo.TipoDocumento;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl extends AbstractDAO implements ClienteDAO {

    private static final String TABLA = "cliente";
    private static final String COLUMNAS =
        "id_cliente, tipo_documento, numero_documento, nombre_razon_social, telefono, activo";
    private static final int LIMITE_BUSQUEDA = 20;
    
    @Override
    public void insertar(Cliente c) {
        String sql = "INSERT INTO " + TABLA +
            " (tipo_documento, numero_documento, nombre_razon_social, telefono, activo) VALUES (?, ?, ?, ?, ?) RETURNING id_cliente";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getTipoDocumento().name());
            ps.setString(2, c.getNumeroDocumento());
            ps.setString(3, c.getNombreRazonSocial());
            ps.setString(4, c.getTelefono());
            ps.setBoolean(5, c.isActivo());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new DaoException("No se pudo insertar el cliente");
                c.setIdCliente(rs.getInt("id_cliente"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar cliente", e);
        } finally { cerrar(cn); }
    }

    @Override
    public void actualizar(Cliente c) {
        String sql = "UPDATE " + TABLA + " SET tipo_documento = ?, numero_documento = ?, nombre_razon_social = ?, telefono = ?, activo = ? " +
            "WHERE id_cliente = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getTipoDocumento().name());
            ps.setString(2, c.getNumeroDocumento());
            ps.setString(3, c.getNombreRazonSocial());
            ps.setString(4, c.getTelefono());
            ps.setBoolean(5, c.isActivo());
            ps.setInt(6, c.getIdCliente());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new DaoException("No existe cliente con id " + c.getIdCliente());
            }
        } catch (SQLException e) {
            throw error("Error al actualizar cliente " + c.getIdCliente(), e);
        } finally { cerrar(cn); }
    }

    @Override
    public void activar(int idCliente) {
        cambiarEstado(idCliente, true);
    }

    @Override
    public void desactivar(int idCliente) {
        cambiarEstado(idCliente, false);
    }

    private void cambiarEstado(int idCliente, boolean activo) {
        String sql = "UPDATE " + TABLA + " SET activo = ? WHERE id_cliente = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new DaoException("No existe cliente con id " + idCliente);
            }
        } catch (SQLException e) {
            throw error("Error al cambiar estado del cliente " + idCliente, e);
        } finally { cerrar(cn); }
    }

    @Override
    public Cliente buscarPorId(Integer id) {
        return buscarPorCampo("id_cliente = ?", id);
    }

    @Override
    public Cliente buscarPorNumeroDocumento(String numeroDocumento) {
        return buscarPorCampo("numero_documento = ?", numeroDocumento);
    }

    private Cliente buscarPorCampo(String condicion, Object valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapear(rs) : null; }
        } catch (SQLException e) {
            throw error("Error al buscar cliente", e);
        } finally { cerrar(cn); }
    }

    @Override
    public List<Cliente> listar() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY nombre_razon_social");
    }

    @Override
    public List<Cliente> listarActivos() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE activo = TRUE ORDER BY nombre_razon_social");
    }
    
    @Override
    public List<Cliente> buscarActivosPorNombreODocumentoParcial(String texto) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA
                + " WHERE activo = TRUE AND (nombre_razon_social ILIKE '%' || ? || '%' "
                + "OR numero_documento ILIKE '%' || ? || '%') "
                + "ORDER BY nombre_razon_social LIMIT ?";
        Connection cn = obtenerConexion();
        List<Cliente> resultado = new ArrayList<>();
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
            throw error("Error al buscar clientes por nombre o documento parcial", e);
        } finally {
            cerrar(cn);
        }
    }

    private List<Cliente> listarConFiltro(String sql) {
        Connection cn = obtenerConexion();
        List<Cliente> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultado.add(mapear(rs));
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar clientes", e);
        } finally { cerrar(cn); }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente(TipoDocumento.valueOf(rs.getString("tipo_documento")),
            rs.getString("numero_documento"), rs.getString("nombre_razon_social"), rs.getString("telefono"));
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }
}