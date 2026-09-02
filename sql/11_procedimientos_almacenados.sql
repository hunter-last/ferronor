-- ============================================================
-- 11_procedimientos_almacenados.sql
-- Decor Home Ferronor — Procedimientos Almacenados y Funciones PL/pgSQL
-- Módulo: Dashboard Principal y Operaciones Transaccionales
-- ============================================================

-- ------------------------------------------------------------
-- 1. PROCEDIMIENTO: Recalcular estados de cuentas vencidas
-- ------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_recalcular_estados_cuentas()
LANGUAGE plpgsql
AS $$
BEGIN
    -- Actualizar Cuentas por Cobrar vencidas
    UPDATE cuenta_cobrar
    SET estado = 'VENCIDA'
    WHERE estado = 'PENDIENTE'
      AND fecha_vencimiento < CURRENT_DATE
      AND saldo_pendiente > 0;

    -- Actualizar Cuentas por Pagar vencidas
    UPDATE cuenta_pagar
    SET estado = 'VENCIDA'
    WHERE estado = 'PENDIENTE'
      AND fecha_vencimiento < CURRENT_DATE
      AND saldo_pendiente > 0;
END;
$$;


-- ------------------------------------------------------------
-- 2. FUNCIÓN: Obtener KPIs consolidados del Dashboard
-- Retorna parámetros OUT con métricas financieras y operativas
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_dashboard_kpis(
    OUT total_ventas_mes NUMERIC(12,2),
    OUT cant_ventas_mes INT,
    OUT total_ventas_hoy NUMERIC(12,2),
    OUT cant_ventas_hoy INT,
    OUT total_compras_mes NUMERIC(12,2),
    OUT cant_compras_mes INT,
    OUT cx_cobrar_pendientes NUMERIC(12,2),
    OUT cx_cobrar_vencidas NUMERIC(12,2),
    OUT cx_pagar_pendientes NUMERIC(12,2),
    OUT cx_pagar_vencidas NUMERIC(12,2),
    OUT saldo_caja_total NUMERIC(12,2),
    OUT saldo_bancos_total NUMERIC(12,2),
    OUT cant_stock_bajo INT,
    OUT cant_stock_agotado INT,
    OUT ordenes_compra_pendientes INT
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Ventas del mes actual (excluyendo canceladas)
    SELECT
        COALESCE(SUM(total), 0.00),
        COUNT(*)
    INTO total_ventas_mes, cant_ventas_mes
    FROM venta
    WHERE date_trunc('month', fecha) = date_trunc('month', CURRENT_DATE)
      AND estado != 'CANCELADA';

    -- Ventas del día de hoy
    SELECT
        COALESCE(SUM(total), 0.00),
        COUNT(*)
    INTO total_ventas_hoy, cant_ventas_hoy
    FROM venta
    WHERE DATE(fecha) = CURRENT_DATE
      AND estado != 'CANCELADA';

    -- Compras del mes actual
    SELECT
        COALESCE(SUM(total), 0.00),
        COUNT(*)
    INTO total_compras_mes, cant_compras_mes
    FROM compra
    WHERE date_trunc('month', fecha) = date_trunc('month', CURRENT_DATE);

    -- Cuentas por Cobrar
    SELECT
        COALESCE(SUM(saldo_pendiente), 0.00),
        COALESCE(SUM(CASE WHEN estado = 'VENCIDA' OR fecha_vencimiento < CURRENT_DATE THEN saldo_pendiente ELSE 0.00 END), 0.00)
    INTO cx_cobrar_pendientes, cx_cobrar_vencidas
    FROM cuenta_cobrar
    WHERE estado IN ('PENDIENTE', 'VENCIDA')
      AND saldo_pendiente > 0;

    -- Cuentas por Pagar
    SELECT
        COALESCE(SUM(saldo_pendiente), 0.00),
        COALESCE(SUM(CASE WHEN estado = 'VENCIDA' OR fecha_vencimiento < CURRENT_DATE THEN saldo_pendiente ELSE 0.00 END), 0.00)
    INTO cx_pagar_pendientes, cx_pagar_vencidas
    FROM cuenta_pagar
    WHERE estado IN ('PENDIENTE', 'VENCIDA')
      AND saldo_pendiente > 0;

    -- Saldo en Cajas Abiertas
    SELECT COALESCE(SUM(saldo_actual), 0.00)
    INTO saldo_caja_total
    FROM caja
    WHERE estado = 'ABIERTA';

    -- Saldo en Cuentas Bancarias Activas
    SELECT COALESCE(SUM(saldo_actual), 0.00)
    INTO saldo_bancos_total
    FROM cuenta_bancaria
    WHERE activa = TRUE;

    -- Alertas de Stock (Bajo nivel y Agotado)
    SELECT
        COUNT(CASE WHEN s.cantidad_actual <= p.stock_minimo THEN 1 END),
        COUNT(CASE WHEN s.cantidad_actual <= 0 THEN 1 END)
    INTO cant_stock_bajo, cant_stock_agotado
    FROM stock s
    JOIN producto p ON s.id_producto = p.id_producto
    WHERE p.activo = TRUE;

    -- Órdenes de Compra Pendientes de Aprobación
    SELECT COUNT(*)
    INTO ordenes_compra_pendientes
    FROM orden_compra
    WHERE estado = 'PENDIENTE';
END;
$$;


-- ------------------------------------------------------------
-- 3. FUNCIÓN: Top productos más vendidos
-- Retorna un conjunto de filas (TABLE) con ranking de ventas
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_dashboard_top_productos(p_limite INT DEFAULT 5)
RETURNS TABLE (
    id_producto INT,
    codigo VARCHAR,
    nombre VARCHAR,
    categoria VARCHAR,
    cantidad_vendida NUMERIC,
    total_recaudado NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id_producto,
        p.codigo,
        p.nombre,
        c.nombre AS categoria,
        COALESCE(SUM(dv.cantidad), 0.00) AS cantidad_vendida,
        COALESCE(SUM(dv.subtotal), 0.00) AS total_recaudado
    FROM detalle_venta dv
    JOIN venta v ON dv.id_venta = v.id_venta
    JOIN producto p ON dv.id_producto = p.id_producto
    JOIN categoria c ON p.id_categoria = c.id_categoria
    WHERE v.estado != 'CANCELADA'
      AND v.fecha >= date_trunc('month', CURRENT_DATE)
    GROUP BY p.id_producto, p.codigo, p.nombre, c.nombre
    ORDER BY cantidad_vendida DESC, total_recaudado DESC
    LIMIT p_limite;
END;
$$;


-- ------------------------------------------------------------
-- 4. FUNCIÓN: Alertas de productos con stock crítico
-- Retorna productos que requieren reabastecimiento urgente
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_dashboard_alertas_stock(p_umbral NUMERIC DEFAULT 0)
RETURNS TABLE (
    id_producto INT,
    codigo VARCHAR,
    nombre VARCHAR,
    categoria VARCHAR,
    unidad VARCHAR,
    stock_actual NUMERIC,
    stock_minimo NUMERIC,
    costo_promedio NUMERIC,
    estado_stock VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id_producto,
        p.codigo,
        p.nombre,
        c.nombre AS categoria,
        u.nombre AS unidad,
        s.cantidad_actual AS stock_actual,
        p.stock_minimo AS stock_minimo,
        s.costo_promedio_actual AS costo_promedio,
        CASE
            WHEN s.cantidad_actual <= 0 THEN 'AGOTADO'::VARCHAR
            WHEN s.cantidad_actual <= p.stock_minimo THEN 'CRITICO'::VARCHAR
            ELSE 'BAJO'::VARCHAR
        END AS estado_stock
    FROM stock s
    JOIN producto p ON s.id_producto = p.id_producto
    JOIN categoria c ON p.id_categoria = c.id_categoria
    JOIN unidad_medida u ON p.id_unidad_medida = u.id_unidad_medida
    WHERE p.activo = TRUE
      AND (s.cantidad_actual <= p.stock_minimo OR (p_umbral > 0 AND s.cantidad_actual <= p_umbral))
    ORDER BY s.cantidad_actual ASC, p.nombre ASC;
END;
$$;


-- ------------------------------------------------------------
-- 5. FUNCIÓN: Últimas ventas registradas
-- Retorna las transacciones más recientes con comprobante y cliente
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_dashboard_ultimas_ventas(p_limite INT DEFAULT 5)
RETURNS TABLE (
    id_venta INT,
    comprobante VARCHAR,
    cliente VARCHAR,
    fecha TIMESTAMP,
    forma_pago VARCHAR,
    total NUMERIC,
    estado VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        v.id_venta,
        COALESCE(tc.codigo || ' ' || comp.serie || '-' || comp.numero, 'SIN COMPROBANTE')::VARCHAR AS comprobante,
        cl.razon_social::VARCHAR AS cliente,
        v.fecha,
        fp.nombre::VARCHAR AS forma_pago,
        v.total,
        v.estado::VARCHAR
    FROM venta v
    JOIN cliente cl ON v.id_cliente = cl.id_cliente
    JOIN forma_pago fp ON v.id_forma_pago = fp.id_forma_pago
    LEFT JOIN comprobante comp ON v.id_venta = comp.id_venta
    LEFT JOIN tipo_comprobante tc ON comp.id_tipo_comprobante = tc.id_tipo_comprobante
    ORDER BY v.fecha DESC, v.id_venta DESC
    LIMIT p_limite;
END;
$$;
