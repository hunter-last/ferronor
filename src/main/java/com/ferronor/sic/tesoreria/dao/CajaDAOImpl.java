package com.ferronor.sic.tesoreria.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.tesoreria.modelo.Caja;
import com.ferronor.sic.tesoreria.modelo.EstadoCaja;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CajaDAOImpl extends AbstractDAO implements CajaDAO {

    private static final String TABLA = "caja";
    private static final String COLUMNAS
            = "id_caja, nombre, saldo_actual, estado, id_usuario_actual, fecha_apertura";

    @Override
    public void insertar(Caja caja) {
        String sql = "INSERT INTO " + TABLA + " (nombre, saldo_actual, estado) VALUES (?, ?, ?) "
                + "RETURNING id_caja";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, caja.getNombre());
            ps.setBigDecimal(2, caja.getSaldoActual());
            ps.setString(3, caja.getEstado().name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar la caja");
                }
                caja.setIdCaja(rs.getInt("id_caja"));
            }
        } catch (SQLException e) {
            throw error("Error al insertar caja", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(Caja caja) {
        String sql = "UPDATE " + TABLA + " SET nombre = ?, saldo_actual = ?, estado = ?, "
                + "id_usuario_actual = ?, fecha_apertura = ? WHERE id_caja = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, caja.getNombre());
            ps.setBigDecimal(2, caja.getSaldoActual());
            ps.setString(3, caja.getEstado().name());
            if (caja.getIdUsuarioActual() != null) {
                ps.setInt(4, caja.getIdUsuarioActual());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (caja.getFechaApertura() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(caja.getFechaApertura()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.setInt(6, caja.getIdCaja());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe caja con id " + caja.getIdCaja());
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar caja " + caja.getIdCaja(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void abrir(int idCaja, int idUsuario) {
        String sql = "UPDATE " + TABLA + " SET estado = ?, id_usuario_actual = ?, fecha_apertura = now() "
                + "WHERE id_caja = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, EstadoCaja.ABIERTA.name());
            ps.setInt(2, idUsuario);
            ps.setInt(3, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe caja con id " + idCaja);
                }
            }
        } catch (SQLException e) {
            throw error("Error al abrir la caja " + idCaja, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void cerrar(int idCaja) {
        String sql = "UPDATE " + TABLA + " SET estado = ?, id_usuario_actual = NULL, fecha_apertura = NULL "
                + "WHERE id_caja = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, EstadoCaja.CERRADA.name());
            ps.setInt(2, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe caja con id " + idCaja);
                }
            }
        } catch (SQLException e) {
            throw error("Error al cerrar la caja " + idCaja, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizarSaldo(int idCaja, BigDecimal nuevoSaldo) {
        String sql = "UPDATE " + TABLA + " SET saldo_actual = ? WHERE id_caja = ? RETURNING 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, nuevoSaldo);
            ps.setInt(2, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe caja con id " + idCaja);
                }
            }
        } catch (SQLException e) {
            throw error("Error al actualizar el saldo de la caja " + idCaja, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Caja buscarPorId(Integer idCaja) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_caja = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar caja " + idCaja, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Optional<Caja> buscarAbierta() {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE estado = ? LIMIT 1";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, EstadoCaja.ABIERTA.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw error("Error al buscar la caja abierta", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Caja> listar() {
        Connection cn = obtenerConexion();
        List<Caja> resultado = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY id_caja";
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar cajas", e);
        } finally {
            cerrar(cn);
        }
    }

    private Caja mapear(ResultSet rs) throws SQLException {
        Caja caja = new Caja();
        caja.setIdCaja(rs.getInt("id_caja"));
        caja.setNombre(rs.getString("nombre"));
        caja.setSaldoActual(rs.getBigDecimal("saldo_actual"));
        caja.setEstado(EstadoCaja.valueOf(rs.getString("estado")));

        int idUsuarioActual = rs.getInt("id_usuario_actual");
        caja.setIdUsuarioActual(rs.wasNull() ? null : idUsuarioActual);

        Timestamp fechaApertura = rs.getTimestamp("fecha_apertura");
        caja.setFechaApertura(fechaApertura != null ? fechaApertura.toLocalDateTime() : null);

        return caja;
    }
}