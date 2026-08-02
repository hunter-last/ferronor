package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.ventas.modelo.Comprobante;
import com.ferronor.sic.ventas.modelo.EstadoComprobante;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComprobanteDAOImpl extends AbstractDAO implements ComprobanteDAO {

    private static final String TABLA = "comprobante";
    private static final String COLUMNAS
            = "id_comprobante, id_venta, id_tipo_comprobante, serie, numero, fecha_emision, estado";

    @Override
    public void insertar(Comprobante comprobante) {
        String sql = "INSERT INTO " + TABLA
                + " (id_venta, id_tipo_comprobante, serie, numero, fecha_emision, estado) "
                + "VALUES (?, ?, ?, ?, now(), ?) RETURNING id_comprobante, fecha_emision";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, comprobante.getIdVenta());
            ps.setInt(2, comprobante.getIdTipoComprobante());
            ps.setString(3, comprobante.getSerie());
            ps.setString(4, comprobante.getNumero());
            ps.setString(5, comprobante.getEstado().name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el comprobante");
                }
                comprobante.setIdComprobante(rs.getInt("id_comprobante"));
                comprobante.setFechaEmision(rs.getTimestamp("fecha_emision").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar comprobante", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Comprobante buscarPorId(Integer idComprobante) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_comprobante = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idComprobante);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar comprobante " + idComprobante, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Comprobante buscarPorVenta(int idVenta) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_venta = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar comprobante de la venta " + idVenta, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Comprobante> listar() {
        Connection cn = obtenerConexion();
        List<Comprobante> resultado = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha_emision DESC";
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar comprobantes", e);
        } finally {
            cerrar(cn);
        }
    }

    private Comprobante mapear(ResultSet rs) throws SQLException {
        Comprobante comprobante = new Comprobante();
        comprobante.setIdComprobante(rs.getInt("id_comprobante"));
        comprobante.setIdVenta(rs.getInt("id_venta"));
        comprobante.setIdTipoComprobante(rs.getInt("id_tipo_comprobante"));
        comprobante.setSerie(rs.getString("serie"));
        comprobante.setNumero(rs.getString("numero"));
        comprobante.setFechaEmision(rs.getTimestamp("fecha_emision").toLocalDateTime());
        comprobante.setEstado(EstadoComprobante.valueOf(rs.getString("estado")));
        return comprobante;
    }
}