/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.inventario.logica;

import com.ferronor.sic.inventario.modelo.dto.KardexItem;
import java.time.LocalDate;
import java.util.List;

public interface KardexService {

    List<KardexItem> obtenerKardex(int idProducto, LocalDate desde, LocalDate hasta);
}
