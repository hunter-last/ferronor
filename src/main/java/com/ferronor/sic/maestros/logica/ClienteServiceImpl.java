/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.dao.ClienteDAO;
import com.ferronor.sic.maestros.modelo.Cliente;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public class ClienteServiceImpl implements ClienteService {

    private final ClienteDAO clienteDAO;

    public ClienteServiceImpl(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(Cliente c) {
        RespuestaOperacion<Void> v = validarComun(c);
        if (!v.isExito()) {
            return v;
        }
        if (clienteDAO.buscarPorNumeroDocumento(c.getNumeroDocumento()) != null) {
            return RespuestaOperacion.error("Ya existe un cliente con ese número de documento");
        }
        clienteDAO.insertar(c);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(Cliente c) {
        RespuestaOperacion<Void> v = validarComun(c);
        if (!v.isExito()) {
            return v;
        }
        if (c.getIdCliente() <= 0) {
            return RespuestaOperacion.error("El cliente es inválido");
        }
        if (clienteDAO.buscarPorId(c.getIdCliente()) == null) {
            return RespuestaOperacion.error("El cliente no existe");
        }
        Cliente conMismoDocumento = clienteDAO.buscarPorNumeroDocumento(c.getNumeroDocumento());
        if (conMismoDocumento != null && conMismoDocumento.getIdCliente() != c.getIdCliente()) {
            return RespuestaOperacion.error("Ya existe un cliente con ese número de documento");
        }
        clienteDAO.actualizar(c);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> activar(int idCliente) {
        if (idCliente <= 0) {
            return RespuestaOperacion.error("El cliente es inválido");
        }
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) {
            return RespuestaOperacion.error("El cliente no existe");
        }
        if (cliente.isActivo()) {
            return RespuestaOperacion.error("El cliente ya se encuentra activo");
        }
        clienteDAO.activar(idCliente);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> desactivar(int idCliente) {
        if (idCliente <= 0) {
            return RespuestaOperacion.error("El cliente es inválido");
        }
        Cliente cliente = clienteDAO.buscarPorId(idCliente);
        if (cliente == null) {
            return RespuestaOperacion.error("El cliente no existe");
        }
        if (!cliente.isActivo()) {
            return RespuestaOperacion.error("El cliente ya se encuentra desactivado");
        }
        clienteDAO.desactivar(idCliente);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<Cliente> listar() {
        return clienteDAO.listar();
    }

    @Override
    public List<Cliente> listarActivos() {
        return clienteDAO.listarActivos();
    }

    private RespuestaOperacion<Void> validarComun(Cliente c) {
        if (c == null) {
            return RespuestaOperacion.error("El cliente es obligatorio");
        }

        if (c.getTipoDocumento() == null) {
            return RespuestaOperacion.error("El tipo de documento es obligatorio");
        }
        if (c.getNumeroDocumento() == null) {
            return RespuestaOperacion.error("El número de documento es obligatorio");
        }
        c.setNumeroDocumento(c.getNumeroDocumento().trim());

        int longitudEsperada;
        switch (c.getTipoDocumento()) {
            case DNI ->
                longitudEsperada = 8;
            case RUC ->
                longitudEsperada = 11;
            default -> {
                return RespuestaOperacion.error("Tipo de documento no soportado");
            }
        }
        if (!c.getNumeroDocumento().matches("\\d{" + longitudEsperada + "}")) {
            return RespuestaOperacion.error("El " + c.getTipoDocumento() + " debe tener " + longitudEsperada + " dígitos numéricos");
        }

        if (c.getNombreRazonSocial() == null) {
            return RespuestaOperacion.error("El nombre o razón social es obligatorio");
        }
        c.setNombreRazonSocial(c.getNombreRazonSocial().trim());
        if (c.getNombreRazonSocial().isEmpty()) {
            return RespuestaOperacion.error("El nombre o razón social es obligatorio");
        }

        if (c.getTelefono() != null) {
            c.setTelefono(c.getTelefono().trim());
        }

        return RespuestaOperacion.ok();
    }

    @Override
    public Cliente buscarPorId(int idCliente) {
        return clienteDAO.buscarPorId(idCliente);
    }

    @Override
    public Cliente buscarPorNumeroDocumento(String numeroDocumento) {
        return clienteDAO.buscarPorNumeroDocumento(numeroDocumento);
    }
}
