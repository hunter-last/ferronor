/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.dao;

import com.ferronor.sic.contabilidad.modelo.DetalleAsiento;
import com.ferronor.sic.contabilidad.modelo.dto.MovimientoCuenta;
import java.time.LocalDate;
import java.util.List;

public interface DetalleAsientoDAO {

    void insertar(DetalleAsiento detalle);

    List<DetalleAsiento> listarPorAsiento(int idAsiento);

    List<DetalleAsiento> listarPorAsientos(List<Integer> idsAsiento); // batch, para Libro Diario

    List<MovimientoCuenta> listarMovimientosPorCuenta(int idCuenta, LocalDate hasta); // JOIN, para Libro Mayor
}
