package com.ferronor.sic.tesoreria.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;
import com.ferronor.sic.tesoreria.modelo.MovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.OrigenMovimientoBanco;
import com.ferronor.sic.tesoreria.modelo.TipoMovimientoBanco;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MovimientoBancoDAOImpl extends AbstractDAO implements MovimientoBancoDAO {

    private static final String TABLA = "movimiento_banco";
    private static final String COLUMNAS
            = "id_movimiento, id_cuenta_bancaria, fecha, tipo, origen, id_documento_origen, monto, "
            + "numero_operacion, id_usuario";

    @Override
    public void insertar(MovimientoBanco mov) {
        String sql = "INSERT INTO " + TABLA
                + " (id_cuenta_bancaria, fecha, tipo, origen, id_documento_origen, monto, numero_operacion, "
                + "id_usuario) VALUES (?, now(), ?, ?, ?, ?, ?, ?) RETURNING id_movimiento, fecha";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, mov.getIdCuentaBancaria());
            ps.setString(2, mov.getTipo().name());
            ps.setString(3, mov.getOrigen().name());
            if (mov.getIdDocumentoOrigen() != null) {
                ps.setInt(4, mov.getIdDocumentoOrigen());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setBigDecimal(5, mov.getMonto());
            ps.setString(6, mov.getNumeroOperacion());
            ps.setInt(7, mov.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar el movimiento bancario");
                }
                mov.setIdMovimiento(rs.getInt("id_movimiento"));
                mov.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar movimiento bancario", e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public MovimientoBanco buscarPorId(Integer idMovimiento) {
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_movimiento = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMovimiento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar movimiento bancario " + idMovimiento, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<MovimientoBanco> listar() {
        return ejecutarListado("SELECT " + COLUMNAS + " FROM " + TABLA + " ORDER BY fecha DESC");
    }

    @Override
    public List<MovimientoBanco> listarPorCuenta(int idCuentaBancaria) {
        Connection cn = obtenerConexion();
        String sql = "SELECT " + COLUMNAS + " FROM " + TABLA + " WHERE id_cuenta_bancaria = ? ORDER BY fecha DESC";
        List<MovimientoBanco> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCuentaBancaria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar movimientos de la cuenta bancaria " + idCuentaBancaria, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<MovimientoBanco> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<MovimientoBanco> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar movimientos bancarios", e);
        } finally {
            cerrar(cn);
        }
    }

    private MovimientoBanco mapear(ResultSet rs) throws SQLException {
        MovimientoBanco mov = new MovimientoBanco();
        mov.setIdMovimiento(rs.getInt("id_movimiento"));
        mov.setIdCuentaBancaria(rs.getInt("id_cuenta_bancaria"));
        mov.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        mov.setTipo(TipoMovimientoBanco.valueOf(rs.getString("tipo")));
        mov.setOrigen(OrigenMovimientoBanco.valueOf(rs.getString("origen")));

        int idDocumentoOrigen = rs.getInt("id_documento_origen");
        mov.setIdDocumentoOrigen(rs.wasNull() ? null : idDocumentoOrigen);

        mov.setMonto(rs.getBigDecimal("monto"));
        mov.setNumeroOperacion(rs.getString("numero_operacion"));
        mov.setIdUsuario(rs.getInt("id_usuario"));
        return mov;
    }
}