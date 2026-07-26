/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.modelo.Cliente;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface ClienteService {

    RespuestaOperacion<Void> registrar(Cliente cliente);

    RespuestaOperacion<Void> actualizar(Cliente cliente);

    RespuestaOperacion<Void> activar(int idCliente);

    RespuestaOperacion<Void> desactivar(int idCliente);

    List<Cliente> listar();

    List<Cliente> listarActivos();

    Cliente buscarPorId(int idCliente);

    Cliente buscarPorNumeroDocumento(String numeroDocumento);
}
