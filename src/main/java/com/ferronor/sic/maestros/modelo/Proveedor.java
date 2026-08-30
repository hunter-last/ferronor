package com.ferronor.sic.maestros.modelo;

public class Proveedor {

    private int idProveedor;
    private String razonSocial;
    private String ruc;
    private String direccion;
    private String telefono;
    private String contacto;
    private boolean activo;

    public Proveedor() {
    }

    public Proveedor(String razonSocial, String ruc, String direccion, String telefono, String contacto) {
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.direccion = direccion;
        this.telefono = telefono;
        this.contacto = contacto;
        this.activo = true;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int id) {
        this.idProveedor = id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    @Override
    public String toString() {
        return razonSocial;
    }
}
