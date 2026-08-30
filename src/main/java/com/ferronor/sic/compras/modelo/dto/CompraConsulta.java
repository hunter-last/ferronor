package com.ferronor.sic.compras.modelo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CompraConsulta {

    private final int idCompra;
    private final Integer idOrdenCompra;
    private final LocalDateTime fecha;

    private final int idProveedor;
    private final String razonSocialProveedor;
    private final String rucProveedor;

    private final int idFormaPago;
    private final String nombreFormaPago;
    private final Integer plazoDias;

    private final String numeroFactura;

    private final BigDecimal subtotal;
    private final BigDecimal igv;
    private final BigDecimal total;

    private final int idUsuario;
    private final String nombreUsuario;

    public CompraConsulta(
            int idCompra,
            Integer idOrdenCompra,
            LocalDateTime fecha,
            int idProveedor,
            String razonSocialProveedor,
            String rucProveedor,
            int idFormaPago,
            String nombreFormaPago,
            Integer plazoDias,
            String numeroFactura,
            BigDecimal subtotal,
            BigDecimal igv,
            BigDecimal total,
            int idUsuario,
            String nombreUsuario) {

        this.idCompra = idCompra;
        this.idOrdenCompra = idOrdenCompra;
        this.fecha = fecha;

        this.idProveedor = idProveedor;
        this.razonSocialProveedor = razonSocialProveedor;
        this.rucProveedor = rucProveedor;

        this.idFormaPago = idFormaPago;
        this.nombreFormaPago = nombreFormaPago;
        this.plazoDias = plazoDias;

        this.numeroFactura = numeroFactura;

        this.subtotal = subtotal;
        this.igv = igv;
        this.total = total;

        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public Integer getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public String getRazonSocialProveedor() {
        return razonSocialProveedor;
    }

    public String getRucProveedor() {
        return rucProveedor;
    }

    public int getIdFormaPago() {
        return idFormaPago;
    }

    public String getNombreFormaPago() {
        return nombreFormaPago;
    }

    public Integer getPlazoDias() {
        return plazoDias;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public boolean tieneOrdenCompra() {
        return idOrdenCompra != null;
    }
}
