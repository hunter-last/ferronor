package com.ferronor.sic.ventas.modelo;

import java.time.LocalDateTime;

public class Comprobante {

    private int idComprobante;
    private int idVenta;
    private int idTipoComprobante;
    private String serie; // copia congelada de tipo_comprobante.serie al momento de emisión
    private String numero; // asignado atómicamente vía correlativo_comprobante
    private LocalDateTime fechaEmision;
    private EstadoComprobante estado;

    public Comprobante() {
    }

    public Comprobante(int idVenta, int idTipoComprobante, String serie, String numero) {
        this.idVenta = idVenta;
        this.idTipoComprobante = idTipoComprobante;
        this.serie = serie;
        this.numero = numero;
        this.estado = EstadoComprobante.EMITIDO;
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(int idComprobante) {
        this.idComprobante = idComprobante;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public int getIdTipoComprobante() {
        return idTipoComprobante;
    }

    public void setIdTipoComprobante(int idTipoComprobante) {
        this.idTipoComprobante = idTipoComprobante;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public EstadoComprobante getEstado() {
        return estado;
    }

    public void setEstado(EstadoComprobante estado) {
        this.estado = estado;
    }
}