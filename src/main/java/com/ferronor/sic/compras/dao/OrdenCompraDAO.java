package com.ferronor.sic.compras.dao;

import com.ferronor.sic.compras.modelo.EstadoOrdenCompra;
import com.ferronor.sic.compras.modelo.OrdenCompra;
import com.ferronor.sic.shared.IGeneralDAO;
import java.util.List;

public interface OrdenCompraDAO extends IGeneralDAO<OrdenCompra, Integer> {

    List<OrdenCompra> listarPorEstado(EstadoOrdenCompra estado);

    void cambiarEstado(int idOrdenCompra, EstadoOrdenCompra estado, int idUsuarioAprueba);
    
    List<OrdenCompra> listarDisponiblesParaCompra();
}