package com.ferronor.sic.dashboard.modelo.dto;

import java.math.BigDecimal;

public class TopProductoDTO {

    private int idProducto;
    private String codigo;
    private String nombre;
    private String categoria;
    private BigDecimal cantidadVendida = BigDecimal.ZERO;
    private BigDecimal totalRecaudado = BigDecimal.ZERO;

    public TopProductoDTO() {
    }

    public TopProductoDTO(int idProducto, String codigo, String nombre, String categoria, BigDecimal cantidadVendida, BigDecimal totalRecaudado) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.cantidadVendida = cantidadVendida != null ? cantidadVendida : BigDecimal.ZERO;
        this.totalRecaudado = totalRecaudado != null ? totalRecaudado : BigDecimal.ZERO;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(BigDecimal cantidadVendida) {
        this.cantidadVendida = cantidadVendida != null ? cantidadVendida : BigDecimal.ZERO;
    }

    public BigDecimal getTotalRecaudado() {
        return totalRecaudado;
    }

    public void setTotalRecaudado(BigDecimal totalRecaudado) {
        this.totalRecaudado = totalRecaudado != null ? totalRecaudado : BigDecimal.ZERO;
    }
}
