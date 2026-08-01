package com.ferronor.sic.tesoreria.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.tesoreria.modelo.MovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoCaja;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoCaja;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MovimientoCajaDAOImpl extends AbstractDAO implements MovimientoCajaDAO {

    private static final String TABLA = "movimiento_caja";
    private static final String COLUMNAS
            = "id_movimiento, id_caja, fecha, tipo, origen, id_documento_origen, monto, descripcion, id_usuario";

    @Override
    public void insertar(MovimientoCaja mov) {
        String sql = "INSERT INTO " + TABLA
                + " (id_caja, fecha, tipo, origen, id_documento_origen, monto, descripcion, id_usuario) "
                + "VALUES (?, now(), ?, ?, ?, ?, ?, ?) RETURNING id_movimiento, fecha";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, mov.getIdCaja());
            ps.setString(2, mov.getTipo().name());
            ps.setString(3, mov.getOrigen().name());
            if (mov.getIdDocumentoOrigen() != null) {
                ps.setInt(4, mov.getIdDocumentoOrigen());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setBigDecimal(5, mov.getMonto());
            ps.setString(6, mov.getDescripcion());
            ps.setInt(7, mov.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el movimiento de caja");
                }
                mov.setIdMovimiento(rs.getInt("id_movimiento"));
                mov.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar movimiento de caja", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public MovimientoCaja buscarPorId(Integer idMovimiento) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_movimiento = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMovimiento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar movimiento de caja " + idMovimiento, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<MovimientoCaja> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha DESC");
    }

    @Override
    public List<MovimientoCaja> listarPorCaja(int idCaja) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_caja = ? ORDER BY fecha DESC";
        List<MovimientoCaja> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar movimientos de la caja " + idCaja, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<MovimientoCaja> listarPorCajaDesde(int idCaja, java.time.LocalDateTime desde) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA
                + " WHERE id_caja = ? AND fecha >= ? ORDER BY fecha";
        List<MovimientoCaja> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(desde));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar movimientos de la caja " + idCaja + " desde " + desde, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<MovimientoCaja> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<MovimientoCaja> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar movimientos de caja", e);
        } finally {
            cerrar(cn);
        }
    }

    private MovimientoCaja mapear(ResultSet rs) throws SQLException {
        MovimientoCaja mov = new MovimientoCaja();
        mov.setIdMovimiento(rs.getInt("id_movimiento"));
        mov.setIdCaja(rs.getInt("id_caja"));
        mov.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        mov.setTipo(TipoMovimientoCaja.valueOf(rs.getString("tipo")));
        mov.setOrigen(OrigenMovimientoCaja.valueOf(rs.getString("origen")));

        int idDocumentoOrigen = rs.getInt("id_documento_origen");
        mov.setIdDocumentoOrigen(rs.wasNull() ? null : idDocumentoOrigen);

        mov.setMonto(rs.getBigDecimal("monto"));
        mov.setDescripcion(rs.getString("descripcion"));
        mov.setIdUsuario(rs.getInt("id_usuario"));
        return mov;
    }
}