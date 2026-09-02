package com.ferronor.sic.dashboard.dao;

import com.ferronor.sic.dashboard.modelo.dto.AlertaStockDTO;
import com.ferronor.sic.dashboard.modelo.dto.DashboardKpisDTO;
import com.ferronor.sic.dashboard.modelo.dto.TopProductoDTO;
import com.ferronor.sic.dashboard.modelo.dto.UltimaVentaDTO;
import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.dao.AbstractDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAOPA extends AbstractDAO {

    /**
     * Ejecuta el Procedimiento Almacenado CALL sp_recalcular_estados_cuentas()
     * para actualizar a 'VENCIDA' las cuentas vencidas pendientes.
     */
    public void recalcularEstadosCuentas() {
        String sql = "CALL sp_recalcular_estados_cuentas()";
        Connection cn = obtenerConexion();
        try (CallableStatement cs = cn.prepareCall(sql)) {
            cs.execute();
        } catch (SQLException e) {
            throw error("Error al ejecutar procedimiento sp_recalcular_estados_cuentas", e);
        } finally {
            cerrar(cn);
        }
    }

    /**
     * Invoca la función PL/pgSQL fn_dashboard_kpis() con parámetros OUT
     * para consolidar en un solo viaje de red todas las métricas del dashboard.
     */
    public DashboardKpisDTO obtenerKpis() {
        String sql = "SELECT * FROM fn_dashboard_kpis()";
        Connection cn = obtenerConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                DashboardKpisDTO kpis = new DashboardKpisDTO();
                kpis.setTotalVentasMes(rs.getBigDecimal("total_ventas_mes"));
                kpis.setCantVentasMes(rs.getInt("cant_ventas_mes"));
                kpis.setTotalVentasHoy(rs.getBigDecimal("total_ventas_hoy"));
                kpis.setCantVentasHoy(rs.getInt("cant_ventas_hoy"));
                kpis.setTotalComprasMes(rs.getBigDecimal("total_compras_mes"));
                kpis.setCantComprasMes(rs.getInt("cant_compras_mes"));
                kpis.setCxCobrarPendientes(rs.getBigDecimal("cx_cobrar_pendientes"));
                kpis.setCxCobrarVencidas(rs.getBigDecimal("cx_cobrar_vencidas"));
                kpis.setCxPagarPendientes(rs.getBigDecimal("cx_pagar_pendientes"));
                kpis.setCxPagarVencidas(rs.getBigDecimal("cx_pagar_vencidas"));
                kpis.setSaldoCajaTotal(rs.getBigDecimal("saldo_caja_total"));
                kpis.setSaldoBancosTotal(rs.getBigDecimal("saldo_bancos_total"));
                kpis.setCantStockBajo(rs.getInt("cant_stock_bajo"));
                kpis.setCantStockAgotado(rs.getInt("cant_stock_agotado"));
                kpis.setOrdenesCompraPendientes(rs.getInt("ordenes_compra_pendientes"));
                return kpis;
            }
            return new DashboardKpisDTO();
        } catch (SQLException e) {
            throw error("Error al invocar función fn_dashboard_kpis", e);
        } finally {
            cerrar(cn);
        }
    }

    /**
     * Invoca la función PL/pgSQL fn_dashboard_top_productos(?)
     * que retorna un conjunto de filas (TABLE) con los productos más vendidos.
     */
    public List<TopProductoDTO> obtenerTopProductos(int limite) {
        String sql = "SELECT * FROM fn_dashboard_top_productos(?)";
        Connection cn = obtenerConexion();
        List<TopProductoDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite > 0 ? limite : 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopProductoDTO item = new TopProductoDTO(
                            rs.getInt("id_producto"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("categoria"),
                            rs.getBigDecimal("cantidad_vendida"),
                            rs.getBigDecimal("total_recaudado")
                    );
                    lista.add(item);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw error("Error al invocar función fn_dashboard_top_productos", e);
        } finally {
            cerrar(cn);
        }
    }

    /**
     * Invoca la función PL/pgSQL fn_dashboard_alertas_stock(?)
     * que retorna un conjunto de filas (TABLE) con los productos en nivel crítico.
     */
    public List<AlertaStockDTO> obtenerAlertasStock(double umbral) {
        String sql = "SELECT * FROM fn_dashboard_alertas_stock(?)";
        Connection cn = obtenerConexion();
        List<AlertaStockDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDouble(1, umbral);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AlertaStockDTO item = new AlertaStockDTO(
                            rs.getInt("id_producto"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("categoria"),
                            rs.getString("unidad"),
                            rs.getBigDecimal("stock_actual"),
                            rs.getBigDecimal("stock_minimo"),
                            rs.getBigDecimal("costo_promedio"),
                            rs.getString("estado_stock")
                    );
                    lista.add(item);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw error("Error al invocar función fn_dashboard_alertas_stock", e);
        } finally {
            cerrar(cn);
        }
    }

    /**
     * Invoca la función PL/pgSQL fn_dashboard_ultimas_ventas(?)
     * que retorna las transacciones más recientes realizadas en el sistema.
     */
    public List<UltimaVentaDTO> obtenerUltimasVentas(int limite) {
        String sql = "SELECT * FROM fn_dashboard_ultimas_ventas(?)";
        Connection cn = obtenerConexion();
        List<UltimaVentaDTO> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite > 0 ? limite : 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha");
                    UltimaVentaDTO item = new UltimaVentaDTO(
                            rs.getInt("id_venta"),
                            rs.getString("comprobante"),
                            rs.getString("cliente"),
                            ts != null ? ts.toLocalDateTime() : null,
                            rs.getString("forma_pago"),
                            rs.getBigDecimal("total"),
                            rs.getString("estado")
                    );
                    lista.add(item);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw error("Error al invocar función fn_dashboard_ultimas_ventas", e);
        } finally {
            cerrar(cn);
        }
    }
}
