
package com.ferronor.sic.maestros.modelo;

public class FormaPago {

    private int idFormaPago;
    private String nombre;
    private boolean esCredito;

    public FormaPago() {
    }

    public FormaPago(String nombre, boolean esCredito) {
        this.nombre = nombre;
        this.esCredito = esCredito;
    }

    public int getIdFormaPago() {
        return idFormaPago;
    }

    public void setIdFormaPago(int id) {
        this.idFormaPago = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEsCredito() {
        return esCredito;
    }

    public void setEsCredito(boolean esCredito) {
        this.esCredito = esCredito;
    }

    @Override
    public String toString() {
        return nombre;
    }
    
}
