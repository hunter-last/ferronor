
package com.ferronor.sic.contabilidad.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AsientoContable {

    private int idAsiento;
    private LocalDateTime fecha;
    private OrigenAsiento origen;
    private int idDocumentoOrigen;
    private String glosa;
    private EstadoAsiento estado;
    private int idUsuario;
    private final List<DetalleAsiento> detalles = new ArrayList<>();

    public AsientoContable() {
    }

    public AsientoContable(OrigenAsiento origen, int idDocumentoOrigen, String glosa, int idUsuario) {
        this.origen = origen;
        this.idDocumentoOrigen = idDocumentoOrigen;
        this.glosa = glosa;
        this.idUsuario = idUsuario;
        this.estado = EstadoAsiento.ACTIVO;
    }

    public void agregarDetalle(DetalleAsiento detalle) {
        detalles.add(detalle);
    }

    public List<DetalleAsiento> getDetalles() {
        return detalles;
    }

    public int getIdAsiento() {
        return idAsiento;
    }

    public void setIdAsiento(int idAsiento) {
        this.idAsiento = idAsiento;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public OrigenAsiento getOrigen() {
        return origen;
    }

    public int getIdDocumentoOrigen() {
        return idDocumentoOrigen;
    }

    public String getGlosa() {
        return glosa;
    }

    public EstadoAsiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoAsiento estado) {
        this.estado = estado;
    }

    public int getIdUsuario() {
        return idUsuario;
    }
}
