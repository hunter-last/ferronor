package com.ferronor.sic.inventario.dao;

import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.inventario.modelo.Stock;
import com.ferronor.sic.inventario.modelo.dto.StockConsulta;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StockDAOImpl extends AbstractDAO implements StockDAO {

    @Override
    public void insertar(Stock stock) {
        String sql = "INSERT INTO stock (id_producto, cantidad_actual, costo_promedio_actual, fecha_ultima_actualizacion) "
                + "VALUES (?, ?, ?, now()) RETURNING fecha_ultima_actualizacion";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, stock.getIdProducto());
            ps.setBigDecimal(2, stock.getCantidadActual());
            ps.setBigDecimal(3, stock.getCostoPromedioActual());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No se pudo insertar stock del producto " + stock.getIdProducto());
                }
                stock.setFechaUltimaActualizacion(rs.getTimestamp("fecha_ultima_actualizacion").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al insertar stock del producto " + stock.getIdProducto(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public void actualizar(Stock stock) {
        String sql = "UPDATE stock SET cantidad_actual = ?, costo_promedio_actual = ?, fecha_ultima_actualizacion = now() "
                + "WHERE id_producto = ? RETURNING fecha_ultima_actualizacion";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, stock.getCantidadActual());
            ps.setBigDecimal(2, stock.getCostoPromedioActual());
            ps.setInt(3, stock.getIdProducto());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DaoException("No existe stock para el producto " + stock.getIdProducto());
                }
                stock.setFechaUltimaActualizacion(rs.getTimestamp("fecha_ultima_actualizacion").toLocalDateTime());
            }
        } catch (SQLException e) {
            throw error("Error al actualizar stock del producto " + stock.getIdProducto(), e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public Stock buscarPorId(Integer idProducto) {
        String sql = "SELECT id_producto, cantidad_actual, costo_promedio_actual, fecha_ultima_actualizacion "
                + "FROM stock WHERE id_producto = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al buscar stock del producto " + idProducto, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<Stock> listar() {
        return ejecutarListado(
                "SELECT id_producto, cantidad_actual, costo_promedio_actual, fecha_ultima_actualizacion FROM stock"
        );
    }

    @Override
    public List<Stock> listarConStockBajo() {
        return ejecutarListado(
                "SELECT s.id_producto, s.cantidad_actual, s.costo_promedio_actual, s.fecha_ultima_actualizacion "
                + "FROM stock s JOIN producto p ON s.id_producto = p.id_producto "
                + "WHERE s.cantidad_actual <= p.stock_minimo AND p.activo = TRUE"
        );
    }

    @Override
    public boolean existeParaProducto(int idProducto) {
        String sql = "SELECT 1 FROM stock WHERE id_producto = ?";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw error("Error al verificar stock del producto " + idProducto, e);
        } finally {
            cerrar(cn);
        }
    }

    @Override
    public List<StockConsulta> consultarTodos() {
        String sql = "SELECT " + "p.id_producto, " + "p.codigo, " + "p.nombre, " + "c.nombre AS nombre_categoria, " + "um.abreviatura, " + "COALESCE(s.cantidad_actual, 0) AS cantidad_actual, " + "p.stock_minimo, " + "COALESCE(s.costo_promedio_actual, 0) AS costo_promedio_actual, " + "s.fecha_ultima_actualizacion, " + "p.activo " + "FROM producto p " + "JOIN categoria c " + "ON c.id_categoria = p.id_categoria " + "JOIN unidad_medida um " + "ON um.id_unidad_medida = p.id_unidad_medida " + "LEFT JOIN stock s " + "ON s.id_producto = p.id_producto " + "WHERE p.activo = TRUE " + "ORDER BY " + "CASE " + "WHEN COALESCE(s.cantidad_actual, 0) = 0 THEN 0 " + "WHEN COALESCE(s.cantidad_actual, 0) <= p.stock_minimo THEN 1 " + "ELSE 2 " + "END, " + "p.codigo ASC";
        return ejecutarListadoConsulta(sql);
    }

    @Override
    public StockConsulta consultarPorProducto(int idProducto) {
        String sql = "SELECT " + "p.id_producto, " + "p.codigo, " + "p.nombre, " + "c.nombre AS nombre_categoria, " + "um.abreviatura, " + "COALESCE(s.cantidad_actual, 0) AS cantidad_actual, " + "p.stock_minimo, " + "COALESCE(s.costo_promedio_actual, 0) AS costo_promedio_actual, " + "s.fecha_ultima_actualizacion, " + "p.activo " + "FROM producto p " + "JOIN categoria c " + "ON c.id_categoria = p.id_categoria " + "JOIN unidad_medida um " + "ON um.id_unidad_medida = p.id_unidad_medida " + "LEFT JOIN stock s " + "ON s.id_producto = p.id_producto " + "WHERE p.id_producto = ? " + "AND p.activo = TRUE";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearStockConsulta(rs) : null;
            }
        } catch (SQLException e) {
            throw error("Error al consultar stock del producto " + idProducto, e);
        } finally {
            cerrar(cn);
        }
    }

    private List<Stock> ejecutarListado(String sql) {
        Connection cn = obtenerConexion();
        List<Stock> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al listar stock", e);
        } finally {
            cerrar(cn);
        }
    }

    private List<StockConsulta> ejecutarListadoConsulta(String sql) {
        Connection cn = obtenerConexion();
        List<StockConsulta> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapearStockConsulta(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al consultar existencias", e);
        } finally {
            cerrar(cn);
        }
    }

    private StockConsulta mapearStockConsulta(ResultSet rs) throws SQLException {
        return new StockConsulta(
                rs.getInt("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("nombre_categoria"),
                rs.getString("abreviatura"),
                rs.getBigDecimal("cantidad_actual"),
                rs.getBigDecimal("stock_minimo"),
                rs.getBigDecimal("costo_promedio_actual"),
                rs.getTimestamp(
                        "fecha_ultima_actualizacion"
                ) != null ?
                        rs.getTimestamp("fecha_ultima_actualizacion").toLocalDateTime()
                        : null, rs.getBoolean("activo"));
    }

    private Stock mapear(ResultSet rs) throws SQLException {
        return new Stock(
                rs.getInt("id_producto"),
                rs.getBigDecimal("cantidad_actual"),
                rs.getBigDecimal("costo_promedio_actual"),
                rs.getTimestamp("fecha_ultima_actualizacion").toLocalDateTime()
        );
    }

}
