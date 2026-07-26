/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.shared.IGeneralDAO;
import java.util.List;

public interface ProveedorDAO extends IGeneralDAO<Proveedor, Integer> {

    Proveedor buscarPorRuc(String ruc);

    List<Proveedor> listarActivos();

    void desactivar(int idProveedor);

    void activar(int idProveedor);
}
