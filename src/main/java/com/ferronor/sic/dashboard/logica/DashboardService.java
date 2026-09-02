package com.ferronor.sic.dashboard.logica;

import com.ferronor.sic.dashboard.modelo.dto.AlertaStockDTO;
import com.ferronor.sic.dashboard.modelo.dto.DashboardKpisDTO;
import com.ferronor.sic.dashboard.modelo.dto.TopProductoDTO;
import com.ferronor.sic.dashboard.modelo.dto.UltimaVentaDTO;
import com.ferronor.sic.shared.RespuestaOperacion;

import java.util.List;

public interface DashboardService {

    /**
     * Obtiene los KPIs consolidados del sistema invocando la función PL/pgSQL fn_dashboard_kpis.
     */
    RespuestaOperacion<DashboardKpisDTO> obtenerKpis();

    /**
     * Obtiene el ranking de productos más vendidos invocando la función PL/pgSQL fn_dashboard_top_productos.
     */
    RespuestaOperacion<List<TopProductoDTO>> obtenerTopProductos(int limite);

    /**
     * Obtiene los productos con alerta de stock crítico/bajo mediante fn_dashboard_alertas_stock.
     */
    RespuestaOperacion<List<AlertaStockDTO>> obtenerAlertasStock(double umbral);

    /**
     * Obtiene las últimas ventas registradas mediante fn_dashboard_ultimas_ventas.
     */
    RespuestaOperacion<List<UltimaVentaDTO>> obtenerUltimasVentas(int limite);

    /**
     * Ejecuta el procedimiento almacenado sp_recalcular_estados_cuentas para poner al día cuentas vencidas.
     */
    RespuestaOperacion<Void> actualizarEstadosCuentas();
}
