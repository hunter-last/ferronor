/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.dao;

import com.ferronor.sic.maestros.modelo.Cliente;
import com.ferronor.sic.shared.IGeneralDAO;
import java.util.List;

public interface ClienteDAO extends IGeneralDAO<Cliente, Integer> {

    Cliente buscarPorNumeroDocumento(String numeroDocumento);

    List<Cliente> listarActivos();

    void activar(int idCliente);

    void desactivar(int idCliente);
}
