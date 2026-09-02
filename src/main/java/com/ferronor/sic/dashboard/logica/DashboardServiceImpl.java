package com.ferronor.sic.dashboard.logica;

import com.ferronor.sic.dashboard.dao.DashboardDAOPA;
import com.ferronor.sic.dashboard.modelo.dto.AlertaStockDTO;
import com.ferronor.sic.dashboard.modelo.dto.DashboardKpisDTO;
import com.ferronor.sic.dashboard.modelo.dto.TopProductoDTO;
import com.ferronor.sic.dashboard.modelo.dto.UltimaVentaDTO;
import com.ferronor.sic.exception.DaoException;
import com.ferronor.sic.shared.RespuestaOperacion;

import java.util.List;

public class DashboardServiceImpl implements DashboardService {

    private final DashboardDAOPA dashboardDAO;

    public DashboardServiceImpl(DashboardDAOPA dashboardDAO) {
        this.dashboardDAO = dashboardDAO;
    }

    @Override
    public RespuestaOperacion<DashboardKpisDTO> obtenerKpis() {
        try {
            // Ejecutamos el recalculo de cuentas en background si es posible
            try {
                dashboardDAO.recalcularEstadosCuentas();
            } catch (Exception ignored) {
                // Si el procedimiento aún no está instalado, continúa con la obtención de métricas
            }
            DashboardKpisDTO kpis = dashboardDAO.obtenerKpis();
            return RespuestaOperacion.ok(kpis);
        } catch (DaoException e) {
            return RespuestaOperacion.error("Error al obtener indicadores del dashboard: " + e.getMessage());
        } catch (Exception e) {
            return RespuestaOperacion.error("Error inesperado al cargar indicadores: " + e.getMessage());
        }
    }

    @Override
    public RespuestaOperacion<List<TopProductoDTO>> obtenerTopProductos(int limite) {
        try {
            List<TopProductoDTO> lista = dashboardDAO.obtenerTopProductos(limite);
            return RespuestaOperacion.ok(lista);
        } catch (DaoException e) {
            return RespuestaOperacion.error("Error al obtener top productos: " + e.getMessage());
        } catch (Exception e) {
            return RespuestaOperacion.error("Error inesperado al obtener top productos: " + e.getMessage());
        }
    }

    @Override
    public RespuestaOperacion<List<AlertaStockDTO>> obtenerAlertasStock(double umbral) {
        try {
            List<AlertaStockDTO> lista = dashboardDAO.obtenerAlertasStock(umbral);
            return RespuestaOperacion.ok(lista);
        } catch (DaoException e) {
            return RespuestaOperacion.error("Error al obtener alertas de stock: " + e.getMessage());
        } catch (Exception e) {
            return RespuestaOperacion.error("Error inesperado al obtener alertas de stock: " + e.getMessage());
        }
    }

    @Override
    public RespuestaOperacion<List<UltimaVentaDTO>> obtenerUltimasVentas(int limite) {
        try {
            List<UltimaVentaDTO> lista = dashboardDAO.obtenerUltimasVentas(limite);
            return RespuestaOperacion.ok(lista);
        } catch (DaoException e) {
            return RespuestaOperacion.error("Error al obtener últimas ventas: " + e.getMessage());
        } catch (Exception e) {
            return RespuestaOperacion.error("Error inesperado al obtener últimas ventas: " + e.getMessage());
        }
    }

    @Override
    public RespuestaOperacion<Void> actualizarEstadosCuentas() {
        try {
            dashboardDAO.recalcularEstadosCuentas();
            return RespuestaOperacion.ok();
        } catch (DaoException e) {
            return RespuestaOperacion.error("Error al recalcular estados de cuentas: " + e.getMessage());
        } catch (Exception e) {
            return RespuestaOperacion.error("Error inesperado al recalcular estados: " + e.getMessage());
        }
    }
}
