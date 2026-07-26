package com.ferronor.sic.inventario.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.inventario.modelo.MovimientoInventario;
import com.ferronor.sic.inventario.modelo.OrigenMovimiento;
import com.ferronor.sic.inventario.modelo.TipoMovimiento;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioDAOImpl extends AbstractDAO implements MovimientoInventarioDAO {

    private static final String TABLA = "movimiento_inventario";

    private static final String COLUMNAS
            = "id_movimiento, id_producto, fecha, tipo, origen, id_documento_origen, cantidad, costo_unitario, costo_total, id_usuario";

    @Override
    public void insertar(MovimientoInventario mov) {

        String sql = "INSERT INTO " + TABLA + " (id_producto, fecha, tipo, origen, id_documento_origen, cantidad, costo_unitario, costo_total, id_usuario) "
                + "VALUES (?, now(), ?, ?, ?, ?, ?, ?, ?) RETURNING id_movimiento, fecha";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, mov.getIdProducto());
            ps.setString(2, mov.getTipo().name());
            ps.setString(3, mov.getOrigen().name());
            if (mov.getIdDocumentoOrigen() != null) {
                ps.setInt(4, mov.getIdDocumentoOrigen());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setBigDecimal(5, mov.getCantidad());
            ps.setBigDecimal(6, mov.getCostoUnitario());
            ps.setBigDecimal(7, mov.getCostoTotal());
            ps.setInt(8, mov.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el movimiento de inventario del producto " + mov.getIdProducto());
                }
                mov.setIdMovimiento(rs.getInt("id_movimiento"));
                mov.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar movimiento de inventario del producto " + mov.getIdProducto(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public MovimientoInventario buscarPorId(Integer idMovimiento) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_movimiento = ?";

        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMovimiento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar movimiento de inventario " + idMovimiento, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<MovimientoInventario> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha ASC, id_movimiento ASC");
    }

    @Override
    public List<MovimientoInventario> listarPorProducto(int idProducto) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_producto = ? ORDER BY fecha ASC, id_movimiento ASC";
        List<MovimientoInventario> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar movimientos del producto " + idProducto, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<MovimientoInventario> listarPorProductoYFecha(int idProducto, LocalDateTime desde, LocalDateTime hasta) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA
                + " WHERE id_producto = ? AND fecha >= ? AND fecha < ? ORDER BY fecha ASC, id_movimiento ASC";
        List<MovimientoInventario> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setTimestamp(2, Timestamp.valueOf(desde));
            ps.setTimestamp(3, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar movimientos del producto " + idProducto + " en el rango indicado", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<MovimientoInventario> listarHastaFecha(int idProducto, LocalDateTime hasta) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA
                + " WHERE id_producto = ? AND fecha < ? ORDER BY fecha ASC, id_movimiento ASC";
        List<MovimientoInventario> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar movimientos del producto " + idProducto + " hasta la fecha indicada", e);
        } finally {
            cerrar(cn);
        }
    }

    private List<MovimientoInventario> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<MovimientoInventario> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar movimientos de inventario", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void vincularDocumentoOrigen(int idMovimiento, int idDocumentoOrigen) {
        String sql = "UPDATE " + TABLA + " SET id_documento_origen = ? WHERE id_movimiento = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDocumentoOrigen);
            ps.setInt(2, idMovimiento);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DaoException(
                        "No existe movimiento de inventario con id " + idMovimiento);
            }
        } catch (SQLException e) {
            throw error("Error al vincular documento de origen del movimiento " + idMovimiento, e);
        } finally {
            cerrar(cn);
        }
    }

    private MovimientoInventario mapear(ResultSet rs) throws SQLException {
        int valorDoc = rs.getInt("id_documento_origen");
        Integer idDocumentoOrigen = rs.wasNull() ? null : valorDoc;
        // ... pasar idDocumentoOrigen a MovimientoInventario.reconstruir(...)
        return MovimientoInventario.reconstruir(
                rs.getInt("id_movimiento"), rs.getInt("id_producto"),
                rs.getTimestamp("fecha").toLocalDateTime(),
                TipoMovimiento.valueOf(rs.getString("tipo")),
                OrigenMovimiento.valueOf(rs.getString("origen")),
                idDocumentoOrigen, rs.getBigDecimal("cantidad"),
                rs.getBigDecimal("costo_unitario"), rs.getBigDecimal("costo_total"),
                rs.getInt("id_usuario")
        );
    }
}
