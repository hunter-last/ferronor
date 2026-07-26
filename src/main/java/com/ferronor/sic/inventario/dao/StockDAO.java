
package com.ferronor.sic.inventario.dao;

import com.ferronor.sic.inventario.modelo.Stock;
import com.ferronor.sic.shared.IGeneralDAO;
import java.util.List;


public interface StockDAO extends IGeneralDAO<Stock, Integer> {
    List<Stock> listarConStockBajo();
    boolean existeParaProducto(int idProducto);
}