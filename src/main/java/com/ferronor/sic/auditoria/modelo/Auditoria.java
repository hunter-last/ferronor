
package com.ferronor.sic.auditoria.modelo;

import java.time.LocalDateTime;

public class Auditoria {

    private int idAuditoria;
    private int idUsuario;
    private LocalDateTime fechaHora;
    private String tablaAfectada;
    private int idRegistroAfectado;
    private TipoOperacionAuditoria operacion;
    private String descripcion;
    private String nombreEquipo;

    public Auditoria() {
    }

    public Auditoria(int idUsuario, String tablaAfectada, int idRegistroAfectado,
            TipoOperacionAuditoria operacion, String descripcion, String nombreEquipo) {
        this.idUsuario = idUsuario;
        this.tablaAfectada = tablaAfectada;
        this.idRegistroAfectado = idRegistroAfectado;
        this.operacion = operacion;
        this.descripcion = descripcion;
        this.nombreEquipo = nombreEquipo;
    }

    public int getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(int idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public int getIdRegistroAfectado() {
        return idRegistroAfectado;
    }

    public TipoOperacionAuditoria getOperacion() {
        return operacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }
}
