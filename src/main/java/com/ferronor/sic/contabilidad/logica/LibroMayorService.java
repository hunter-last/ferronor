/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.dto.LibroMayorItem;
import java.time.LocalDate;
import java.util.List;

public interface LibroMayorService {

    List<LibroMayorItem> obtenerLibroMayor(int idCuenta, LocalDate desde, LocalDate hasta);
}
