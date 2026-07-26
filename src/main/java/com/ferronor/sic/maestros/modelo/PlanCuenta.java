
package com.ferronor.sic.maestros.modelo;

public class PlanCuenta {

    private int idCuenta;
    private String codigo;
    private String nombreCuenta;
    private Integer idCuentaPadre; // nullable: cuentas raíz no tienen padre
    private int nivel;

    public PlanCuenta() {
    }

    public PlanCuenta(String codigo, String nombreCuenta, Integer idCuentaPadre, int nivel) {
        this.codigo = codigo;
        this.nombreCuenta = nombreCuenta;
        this.idCuentaPadre = idCuentaPadre;
        this.nivel = nivel;
    }

    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int id) {
        this.idCuenta = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public Integer getIdCuentaPadre() {
        return idCuentaPadre;
    }

    public void setIdCuentaPadre(Integer idCuentaPadre) {
        this.idCuentaPadre = idCuentaPadre;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }
}
