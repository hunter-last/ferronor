package com.ferronor.sic.compras.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrdenCompra {

    private int idOrdenCompra;
    private int idProveedor;
    private LocalDateTime fecha;
    private EstadoOrdenCompra estado;
    private int idUsuarioSolicita;
    private Integer idUsuarioAprueba; // nullable hasta que se apruebe/rechace
    private LocalDateTime fechaAprobacion; // nullable
    private final List<DetalleOrdenCompra> detalles = new ArrayList<>();

    public OrdenCompra() {
    }

    public OrdenCompra(int idProveedor, int idUsuarioSolicita) {
        this.idProveedor = idProveedor;
        this.idUsuarioSolicita = idUsuarioSolicita;
        this.estado = EstadoOrdenCompra.PENDIENTE;
    }

    public void agregarDetalle(DetalleOrdenCompra detalle) {
        detalles.add(detalle);
    }

    public List<DetalleOrdenCompra> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public int getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(int idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public EstadoOrdenCompra getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrdenCompra estado) {
        this.estado = estado;
    }

    public int getIdUsuarioSolicita() {
        return idUsuarioSolicita;
    }

    public void setIdUsuarioSolicita(int idUsuarioSolicita) {
        this.idUsuarioSolicita = idUsuarioSolicita;
    }

    public Integer getIdUsuarioAprueba() {
        return idUsuarioAprueba;
    }

    public void setIdUsuarioAprueba(Integer idUsuarioAprueba) {
        this.idUsuarioAprueba = idUsuarioAprueba;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    @Override
    public String toString() {
        return String.format("OC-%04d", idOrdenCompra);
    }
}
