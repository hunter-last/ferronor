
package com.ferronor.sic.auditoria.dao;

import com.ferronor.sic.auditoria.modelo.Auditoria;
import com.ferronor.sic.auditoria.modelo.TipoOperacionAuditoria;
import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAOImpl extends AbstractDAO implements AuditoriaDAO {

    private static final String TABLA = "auditoria";
    private static final String COLUMNAS
            = "id_auditoria, id_usuario, fecha_hora, tabla_afectada, id_registro_afectado, operacion, descripcion, nombre_equipo";

    @Override
    public void insertar(Auditoria a) {
        String sql = "INSERT INTO " + TABLA
                + " (id_usuario, tabla_afectada, id_registro_afectado, operacion, descripcion, nombre_equipo) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_auditoria";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, a.getIdUsuario());
            ps.setString(2, a.getTablaAfectada());
            ps.setInt(3, a.getIdRegistroAfectado());
            ps.setString(4, a.getOperacion().name());
            ps.setString(5, a.getDescripcion());
            ps.setString(6, a.getNombreEquipo());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar auditoría");
                }
                a.setIdAuditoria(rs.getInt("id_auditoria"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar auditoría", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Auditoria> listar() {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha_hora DESC";
        Connection cn = obtenerConexion();
        List<Auditoria> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar auditoría", e);
        } finally {
            cerrar(cn);
        }
    }

    private Auditoria mapear(ResultSet rs) throws SQLException {
        Auditoria a = new Auditoria(
                rs.getInt("id_usuario"), rs.getString("tabla_afectada"), rs.getInt("id_registro_afectado"),
                TipoOperacionAuditoria.valueOf(rs.getString("operacion")), rs.getString("descripcion"), rs.getString("nombre_equipo"));
        a.setIdAuditoria(rs.getInt("id_auditoria"));
        a.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
        return a;
    }
}
