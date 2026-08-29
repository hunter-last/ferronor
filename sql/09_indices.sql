CREATE INDEX idx_movimiento_producto_fecha ON movimiento_inventario(id_producto, fecha);
CREATE INDEX idx_movimiento_fecha ON movimiento_inventario(fecha);
CREATE INDEX idx_movimiento_origen ON movimiento_inventario(origen);
CREATE INDEX idx_ajuste_producto ON ajuste_inventario(id_producto);

CREATE INDEX idx_cuenta_pagar_vencimiento ON cuenta_pagar(fecha_vencimiento) WHERE estado = 'PENDIENTE';

CREATE UNIQUE INDEX uq_compra_orden_compra
ON compra(id_orden_compra)
WHERE id_orden_compra IS NOT NULL;