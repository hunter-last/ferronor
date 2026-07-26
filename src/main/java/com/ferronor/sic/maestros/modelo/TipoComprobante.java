
package com.ferronor.sic.maestros.modelo;

public class TipoComprobante {

    private int idTipoComprobante;
    private String nombre;
    private String serie;

    public TipoComprobante() {
    }

    public TipoComprobante(String nombre, String serie) {
        this.nombre = nombre;
        this.serie = serie;
    }

    public int getIdTipoComprobante() {
        return idTipoComprobante;
    }

    public void setIdTipoComprobante(int id) {
        this.idTipoComprobante = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }
}
