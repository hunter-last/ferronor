/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.maestros.modelo.PlanCuenta;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlanCuentaDAOImpl extends AbstractDAO implements PlanCuentaDAO {

    private static final String TABLA = "plan_cuenta";
    private static final String COLUMNAS = "id_cuenta, codigo, nombre_cuenta, id_cuenta_padre, nivel";

    @Override
    public void insertar(PlanCuenta c) {
        String sql = "INSERT INTO " + TABLA + " (codigo, nombre_cuenta, id_cuenta_padre, nivel) VALUES (?, ?, ?, ?) RETURNING id_cuenta";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getCodigo());
            ps.setString(2, c.getNombreCuenta());
            if (c.getIdCuentaPadre() != null) {
                ps.setInt(3, c.getIdCuentaPadre());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, c.getNivel());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la cuenta contable");
                }
                c.setIdCuenta(rs.getInt("id_cuenta"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar cuenta contable", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(PlanCuenta c) {
        String sql = "UPDATE " + TABLA + " SET codigo = ?, nombre_cuenta = ?, id_cuenta_padre = ?, nivel = ? WHERE id_cuenta = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getCodigo());
            ps.setString(2, c.getNombreCuenta());
            if (c.getIdCuentaPadre() != null) {
                ps.setInt(3, c.getIdCuentaPadre());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, c.getNivel());
            ps.setInt(5, c.getIdCuenta());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe cuenta contable con id " + c.getIdCuenta());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar cuenta contable " + c.getIdCuenta(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public PlanCuenta buscarPorId(Integer id) {
        return buscarPorCampo("id_cuenta = ?", id);
    }

    @Override
    public PlanCuenta buscarPorCodigo(String codigo) {
        return buscarPorCampo("codigo = ?", codigo);
    }

    private PlanCuenta buscarPorCampo(String condicion, Object valor) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE " + condicion;
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar cuenta contable", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<PlanCuenta> listar() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY codigo");
    }

    @Override
    public List<PlanCuenta> listarHijos(int idCuentaPadre) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_cuenta_padre = ? ORDER BY codigo";
        List<PlanCuenta> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCuentaPadre);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar cuentas hijas", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<PlanCuenta> listarRaiz() {
        return listarConFiltro("SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_cuenta_padre IS NULL ORDER BY codigo");
    }

    private List<PlanCuenta> listarConFiltro(String sql) {
        Connection cn = obtenerConexion();
        List<PlanCuenta> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar cuentas contables", e);
        } finally {
            cerrar(cn);
        }
    }

    private PlanCuenta mapear(ResultSet rs) throws SQLException {
        int idPadreRaw = rs.getInt("id_cuenta_padre");
        Integer idCuentaPadre = rs.wasNull() ? null : idPadreRaw;
        PlanCuenta c = new PlanCuenta(rs.getString("codigo"), rs.getString("nombre_cuenta"), idCuentaPadre, rs.getInt("nivel"));
        c.setIdCuenta(rs.getInt("id_cuenta"));
        return c;
    }
}
