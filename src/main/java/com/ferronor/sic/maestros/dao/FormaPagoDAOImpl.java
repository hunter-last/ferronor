/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.maestros.modelo.FormaPago;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FormaPagoDAOImpl extends AbstractDAO implements FormaPagoDAO {

    private static final String TABLA = "forma_pago";
    private static final String COLUMNAS = "id_forma_pago, nombre, es_credito";

    @Override
    public void insertar(FormaPago f) {
        String sql = "INSERT INTO " + TABLA + " (nombre, es_credito) VALUES (?, ?) RETURNING id_forma_pago";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, f.getNombre());
            ps.setBoolean(2, f.isEsCredito());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new DaoException("No se pudo insertar la forma de pago");
                f.setIdFormaPago(rs.getInt("id_forma_pago"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar forma de pago", e);
        } finally { cerrar(cn); }
    }

    @Override
    public void actualizar(FormaPago f) {
        String sql = "UPDATE " + TABLA + " SET nombre = ?, es_credito = ? WHERE id_forma_pago = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, f.getNombre());
            ps.setBoolean(2, f.isEsCredito());
            ps.setInt(3, f.getIdFormaPago());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new DaoException("No existe forma de pago con id " + f.getIdFormaPago());
            }
        } catch (SQLException e) {
            throw error("Error al actualizar forma de pago " + f.getIdFormaPago(), e);
        } finally { cerrar(cn); }
    }

    @Override
    public FormaPago buscarPorId(Integer id) {
        return buscarPorCampo("id_forma_pago = ?", id);
    }

    @Override
    public FormaPago buscarPorNombre(String nombre) {
        return buscarPorCampo("UPPER(nombre) = UPPER(?)", nombre);
    }

    private FormaPago buscarPorCampo(String condicion, Object valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapear(rs) : null; }
        } catch (SQLException e) {
            throw error("Error al buscar forma de pago", e);
        } finally { cerrar(cn); }
    }

    @Override
    public List<FormaPago> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY nombre";
        Connection cn = obtenerConexion();
        List<FormaPago> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) resultado.add(mapear(rs));
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar formas de pago", e);
        } finally { cerrar(cn); }
    }

    private FormaPago mapear(ResultSet rs) throws SQLException {
        FormaPago f = new FormaPago(rs.getString("nombre"), rs.getBoolean("es_credito"));
        f.setIdFormaPago(rs.getInt("id_forma_pago"));
        return f;
    }
}