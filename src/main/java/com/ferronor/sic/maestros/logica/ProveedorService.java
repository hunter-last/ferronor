/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface ProveedorService {

    RespuestaOperacion<Void> registrar(Proveedor proveedor);

    RespuestaOperacion<Void> actualizar(Proveedor proveedor);

    RespuestaOperacion<Void> activar(int idProveedor);

    RespuestaOperacion<Void> desactivar(int idProveedor);

    List<Proveedor> listar();

    List<Proveedor> listarActivos();

    Proveedor buscarPorId(int idProveedor);

    Proveedor buscarPorRuc(String ruc);

    List<Proveedor> buscarActivosPorRazonSocialORucParcial(String texto);
}

