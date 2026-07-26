/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.inventario.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author JEFERSON
 */
public class AjusteInventario {

    private int idAjuste;
    private int idProducto;
    private LocalDateTime fecha;
    private BigDecimal cantidadSistema;
    private BigDecimal cantidadFisica;
    private BigDecimal diferencia;
    private String motivo;
    private int idUsuario;
    private int idMovimientoGenerado;

    public AjusteInventario() {
    }

    public AjusteInventario(int idProducto, BigDecimal cantidadSistema, BigDecimal cantidadFisica,
            String motivo, int idUsuario) {
        this.idProducto = idProducto;
        this.cantidadSistema = cantidadSistema;
        this.cantidadFisica = cantidadFisica;
        this.motivo = motivo;
        this.idUsuario = idUsuario;
        recalcularDiferencia();
    }

    public static AjusteInventario reconstruir(int idAjuste, int idProducto, LocalDateTime fecha,
            BigDecimal cantidadSistema, BigDecimal cantidadFisica, BigDecimal diferencia,
            String motivo, int idUsuario, int idMovimientoGenerado) {
        AjusteInventario a = new AjusteInventario();
        a.idAjuste = idAjuste;
        a.idProducto = idProducto;
        a.fecha = fecha;
        a.cantidadSistema = cantidadSistema;
        a.cantidadFisica = cantidadFisica;
        a.diferencia = diferencia; // valor exacto de la BD, sin recalcular
        a.motivo = motivo;
        a.idUsuario = idUsuario;
        a.idMovimientoGenerado = idMovimientoGenerado;
        return a;
    }

    private void recalcularDiferencia() {
        if (cantidadSistema != null && cantidadFisica != null) {
            this.diferencia = cantidadFisica.subtract(cantidadSistema);
        }
    }

    public int getIdAjuste() {
        return idAjuste;
    }

    public void setIdAjuste(int idAjuste) {
        this.idAjuste = idAjuste;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getCantidadSistema() {
        return cantidadSistema;
    }

    public void setCantidadSistema(BigDecimal cantidadSistema) {
        this.cantidadSistema = cantidadSistema;
        recalcularDiferencia();
    }

    public BigDecimal getCantidadFisica() {
        return cantidadFisica;
    }

    public void setCantidadFisica(BigDecimal cantidadFisica) {
        this.cantidadFisica = cantidadFisica;
        recalcularDiferencia();
    }

    // sin setDiferencia(): es un campo derivado, solo se calcula internamente
    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdMovimientoGenerado() {
        return idMovimientoGenerado;
    }

    public void setIdMovimientoGenerado(int id) {
        this.idMovimientoGenerado = id;
    }

    @Override
    public String toString() {
        return "AjusteInventario{idAjuste=" + idAjuste + ", idProducto=" + idProducto
                + ", cantidadSistema=" + cantidadSistema + ", cantidadFisica=" + cantidadFisica
                + ", diferencia=" + diferencia + ", motivo='" + motivo + "'}";
    }
}
