package com.ferronor.sic.dashboard.modelo.dto;

import java.math.BigDecimal;

public class DashboardKpisDTO {

    private BigDecimal totalVentasMes = BigDecimal.ZERO;
    private int cantVentasMes;
    private BigDecimal totalVentasHoy = BigDecimal.ZERO;
    private int cantVentasHoy;
    private BigDecimal totalComprasMes = BigDecimal.ZERO;
    private int cantComprasMes;
    private BigDecimal cxCobrarPendientes = BigDecimal.ZERO;
    private BigDecimal cxCobrarVencidas = BigDecimal.ZERO;
    private BigDecimal cxPagarPendientes = BigDecimal.ZERO;
    private BigDecimal cxPagarVencidas = BigDecimal.ZERO;
    private BigDecimal saldoCajaTotal = BigDecimal.ZERO;
    private BigDecimal saldoBancosTotal = BigDecimal.ZERO;
    private int cantStockBajo;
    private int cantStockAgotado;
    private int ordenesCompraPendientes;

    public DashboardKpisDTO() {
    }

    public BigDecimal getTotalVentasMes() {
        return totalVentasMes != null ? totalVentasMes : BigDecimal.ZERO;
    }

    public void setTotalVentasMes(BigDecimal totalVentasMes) {
        this.totalVentasMes = totalVentasMes;
    }

    public int getCantVentasMes() {
        return cantVentasMes;
    }

    public void setCantVentasMes(int cantVentasMes) {
        this.cantVentasMes = cantVentasMes;
    }

    public BigDecimal getTotalVentasHoy() {
        return totalVentasHoy != null ? totalVentasHoy : BigDecimal.ZERO;
    }

    public void setTotalVentasHoy(BigDecimal totalVentasHoy) {
        this.totalVentasHoy = totalVentasHoy;
    }

    public int getCantVentasHoy() {
        return cantVentasHoy;
    }

    public void setCantVentasHoy(int cantVentasHoy) {
        this.cantVentasHoy = cantVentasHoy;
    }

    public BigDecimal getTotalComprasMes() {
        return totalComprasMes != null ? totalComprasMes : BigDecimal.ZERO;
    }

    public void setTotalComprasMes(BigDecimal totalComprasMes) {
        this.totalComprasMes = totalComprasMes;
    }

    public int getCantComprasMes() {
        return cantComprasMes;
    }

    public void setCantComprasMes(int cantComprasMes) {
        this.cantComprasMes = cantComprasMes;
    }

    public BigDecimal getCxCobrarPendientes() {
        return cxCobrarPendientes != null ? cxCobrarPendientes : BigDecimal.ZERO;
    }

    public void setCxCobrarPendientes(BigDecimal cxCobrarPendientes) {
        this.cxCobrarPendientes = cxCobrarPendientes;
    }

    public BigDecimal getCxCobrarVencidas() {
        return cxCobrarVencidas != null ? cxCobrarVencidas : BigDecimal.ZERO;
    }

    public void setCxCobrarVencidas(BigDecimal cxCobrarVencidas) {
        this.cxCobrarVencidas = cxCobrarVencidas;
    }

    public BigDecimal getCxPagarPendientes() {
        return cxPagarPendientes != null ? cxPagarPendientes : BigDecimal.ZERO;
    }

    public void setCxPagarPendientes(BigDecimal cxPagarPendientes) {
        this.cxPagarPendientes = cxPagarPendientes;
    }

    public BigDecimal getCxPagarVencidas() {
        return cxPagarVencidas != null ? cxPagarVencidas : BigDecimal.ZERO;
    }

    public void setCxPagarVencidas(BigDecimal cxPagarVencidas) {
        this.cxPagarVencidas = cxPagarVencidas;
    }

    public BigDecimal getSaldoCajaTotal() {
        return saldoCajaTotal != null ? saldoCajaTotal : BigDecimal.ZERO;
    }

    public void setSaldoCajaTotal(BigDecimal saldoCajaTotal) {
        this.saldoCajaTotal = saldoCajaTotal;
    }

    public BigDecimal getSaldoBancosTotal() {
        return saldoBancosTotal != null ? saldoBancosTotal : BigDecimal.ZERO;
    }

    public void setSaldoBancosTotal(BigDecimal saldoBancosTotal) {
        this.saldoBancosTotal = saldoBancosTotal;
    }

    public int getCantStockBajo() {
        return cantStockBajo;
    }

    public void setCantStockBajo(int cantStockBajo) {
        this.cantStockBajo = cantStockBajo;
    }

    public int getCantStockAgotado() {
        return cantStockAgotado;
    }

    public void setCantStockAgotado(int cantStockAgotado) {
        this.cantStockAgotado = cantStockAgotado;
    }

    public int getOrdenesCompraPendientes() {
        return ordenesCompraPendientes;
    }

    public void setOrdenesCompraPendientes(int ordenesCompraPendientes) {
        this.ordenesCompraPendientes = ordenesCompraPendientes;
    }
}
