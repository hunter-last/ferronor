package com.ferronor.sic.ventas.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CorrelativoComprobanteDAOImpl extends AbstractDAO implements CorrelativoComprobanteDAO {

    @Override
    public int obtenerSiguienteNumero(int idTipoComprobante) {
        String sql = "UPDATE correlativo_comprobante SET ultimo_numero = ultimo_numero + 1 "
                + "WHERE id_tipo_comprobante = ? RETURNING ultimo_numero";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idTipoComprobante);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe correlativo para el tipo de comprobante "
                            + idTipoComprobante);
                }
                return rs.getInt("ultimo_numero");
            }
        } catch (SQLException e) {
            throw error("Error al obtener el siguiente correlativo del tipo de comprobante "
                    + idTipoComprobante, e);
        } finally {
            cerrar(cn);
        }
    }
}