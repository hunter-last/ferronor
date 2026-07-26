/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.inventario.dao;

import com.ferronor.sic.inventario.modelo.MovimientoInventario;
import com.ferronor.sic.shared.IHistoricoDAO;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioDAO extends IHistoricoDAO<MovimientoInventario, Integer> {

    List<MovimientoInventario> listarPorProducto(int idProducto);

    List<MovimientoInventario> listarPorProductoYFecha(int idProducto, LocalDateTime desde, LocalDateTime hasta);

    void vincularDocumentoOrigen(int idMovimiento, int idDocumentoOrigen);

    List<MovimientoInventario> listarHastaFecha(int idProducto, LocalDateTime hasta);

}
