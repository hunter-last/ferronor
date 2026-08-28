/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.dao.CategoriaDAO;
import com.ferronor.sic.maestros.dao.ProductoDAO;
import com.ferronor.sic.maestros.dao.UnidadMedidaDAO;
import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.math.BigDecimal;
import java.util.List;

public class ProductoServiceImpl implements ProductoService {

    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;
    private final UnidadMedidaDAO unidadMedidaDAO;

    public ProductoServiceImpl(ProductoDAO productoDAO, CategoriaDAO categoriaDAO, UnidadMedidaDAO unidadMedidaDAO) {
        this.productoDAO = productoDAO;
        this.categoriaDAO = categoriaDAO;
        this.unidadMedidaDAO = unidadMedidaDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(Producto producto) {
        RespuestaOperacion<Void> validacion = validarComun(producto);
        if (!validacion.isExito()) {
            return validacion;
        }
        if (productoDAO.buscarPorCodigo(producto.getCodigo()) != null) {
            return RespuestaOperacion.error("Ya existe un producto con ese código");
        }
        productoDAO.insertar(producto);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(Producto producto) {
        RespuestaOperacion<Void> validacion = validarComun(producto);
        if (!validacion.isExito()) {
            return validacion;
        }
        if (productoDAO.buscarPorId(producto.getIdProducto()) == null) {
            return RespuestaOperacion.error("El producto no existe");
        }
        Producto conMismoCodigo = productoDAO.buscarPorCodigo(producto.getCodigo());
        if (conMismoCodigo != null && conMismoCodigo.getIdProducto() != producto.getIdProducto()) {
            return RespuestaOperacion.error("Ya existe un producto con ese código");
        }
        productoDAO.actualizar(producto);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> activar(int idProducto) {
        if (idProducto <= 0) {
            return RespuestaOperacion.error("El producto es inválido");
        }
        Producto producto = productoDAO.buscarPorId(idProducto);
        if (producto == null) {
            return RespuestaOperacion.error("El producto no existe");
        }
        if (producto.isActivo()) {
            return RespuestaOperacion.error("El producto ya se encuentra activo");
        }
        productoDAO.activar(idProducto);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<Producto> listar() {
        return productoDAO.listar();
    }

    @Override
    public RespuestaOperacion<Void> desactivar(int idProducto) {
        if (idProducto <= 0) {
            return RespuestaOperacion.error("El producto es inválido");
        }
        Producto producto = productoDAO.buscarPorId(idProducto);
        if (producto == null) {
            return RespuestaOperacion.error("El producto no existe");
        }
        if (!producto.isActivo()) {
            return RespuestaOperacion.error("El producto ya se encuentra desactivado");
        }
        productoDAO.desactivar(idProducto);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<Producto> listarActivos() {
        return productoDAO.listarActivos();
    }

    private RespuestaOperacion<Void> validarComun(Producto p) {
        RespuestaOperacion<Void> r;
        if (!(r = validarCodigo(p)).isExito()) {
            return r;
        }
        if (!(r = validarNombre(p)).isExito()) {
            return r;
        }
        if (!(r = validarCategoria(p)).isExito()) {
            return r;
        }
        if (!(r = validarUnidadMedida(p)).isExito()) {
            return r;
        }
        if (!(r = validarStockMinimo(p)).isExito()) {
            return r;
        }
        return validarPrecioVenta(p);
    }

    private RespuestaOperacion<Void> validarCodigo(Producto p) {
        if (p.getCodigo() == null || p.getCodigo().isBlank()) {
            return RespuestaOperacion.error("El código del producto es obligatorio");
        }
        p.setCodigo(p.getCodigo().trim().toUpperCase(java.util.Locale.ROOT));
        return RespuestaOperacion.ok();
    }

    private RespuestaOperacion<Void> validarNombre(Producto p) {
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            return RespuestaOperacion.error("El nombre del producto es obligatorio");
        }
        p.setNombre(p.getNombre().trim());
        return RespuestaOperacion.ok();
    }

    private RespuestaOperacion<Void> validarCategoria(Producto p) {
        if (categoriaDAO.buscarPorId(p.getIdCategoria()) == null) {
            return RespuestaOperacion.error("La categoría indicada no existe");
        }
        return RespuestaOperacion.ok();
    }

    private RespuestaOperacion<Void> validarUnidadMedida(Producto p) {
        if (unidadMedidaDAO.buscarPorId(p.getIdUnidadMedida()) == null) {
            return RespuestaOperacion.error("La unidad de medida indicada no existe");
        }
        return RespuestaOperacion.ok();
    }

    private RespuestaOperacion<Void> validarStockMinimo(Producto p) {
        if (p.getStockMinimo() == null) {
            return RespuestaOperacion.error("El stock mínimo es obligatorio");
        }
        if (p.getStockMinimo().compareTo(BigDecimal.ZERO) < 0) {
            return RespuestaOperacion.error("El stock mínimo no puede ser negativo");
        }
        return RespuestaOperacion.ok();
    }

    private RespuestaOperacion<Void> validarPrecioVenta(Producto p) {
        if (p.getPrecioVenta() == null) {
            return RespuestaOperacion.error("El precio de venta es obligatorio");
        }
        if (p.getPrecioVenta().compareTo(BigDecimal.ZERO) < 0) {
            return RespuestaOperacion.error("El precio de venta no puede ser negativo");
        }
        return RespuestaOperacion.ok();
    }

    @Override
    public Producto buscarPorId(int idProducto) {
        return productoDAO.buscarPorId(idProducto);
    }

    @Override
    public Producto buscarPorCodigo(String codigo) {
        return productoDAO.buscarPorCodigo(codigo);
    }

    @Override
    public List<Producto> buscarActivosPorNombreOCodigoParcial(String texto) {
        return productoDAO.buscarActivosPorNombreOCodigoParcial(texto);
    }

}
