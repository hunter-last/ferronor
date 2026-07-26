
package com.ferronor.sic.maestros.modelo;

public class UnidadMedida {

    private int idUnidadMedida;
    private String nombre;
    private String abreviatura;

    public UnidadMedida() {
    }

    public UnidadMedida(String nombre, String abreviatura) {
        this.nombre = nombre;
        this.abreviatura = abreviatura;
    }

    public int getIdUnidadMedida() {
        return idUnidadMedida;
    }

    public void setIdUnidadMedida(int id) {
        this.idUnidadMedida = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    @Override
    public String toString() {
        return "UnidadMedida{id=" + idUnidadMedida + ", nombre='" + nombre + "', abreviatura='" + abreviatura + "'}";
    }
}
