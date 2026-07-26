/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.dao.ProveedorDAO;
import com.ferronor.sic.maestros.modelo.Proveedor;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorDAO proveedorDAO;

    public ProveedorServiceImpl(ProveedorDAO proveedorDAO) {
        this.proveedorDAO = proveedorDAO;
    }

    @Override
    public RespuestaOperacion<Void> registrar(Proveedor p) {
        RespuestaOperacion<Void> v = validarComun(p);
        if (!v.isExito()) {
            return v;
        }
        if (proveedorDAO.buscarPorRuc(p.getRuc()) != null) {
            return RespuestaOperacion.error("Ya existe un proveedor con ese RUC");
        }
        proveedorDAO.insertar(p);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> actualizar(Proveedor p) {
        RespuestaOperacion<Void> v = validarComun(p);
        if (!v.isExito()) {
            return v;
        }
        if (p.getIdProveedor() <= 0) {
            return RespuestaOperacion.error("El proveedor es inválido");
        }
        if (proveedorDAO.buscarPorId(p.getIdProveedor()) == null) {
            return RespuestaOperacion.error("El proveedor no existe");
        }
        Proveedor conMismoRuc = proveedorDAO.buscarPorRuc(p.getRuc());
        if (conMismoRuc != null && conMismoRuc.getIdProveedor() != p.getIdProveedor()) {
            return RespuestaOperacion.error("Ya existe un proveedor con ese RUC");
        }
        proveedorDAO.actualizar(p);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> activar(int idProveedor) {
        if (idProveedor <= 0) {
            return RespuestaOperacion.error("El proveedor es inválido");
        }
        Proveedor proveedor = proveedorDAO.buscarPorId(idProveedor);
        if (proveedor == null) {
            return RespuestaOperacion.error("El proveedor no existe");
        }
        if (proveedor.isActivo()) {
            return RespuestaOperacion.error("El proveedor ya se encuentra activo");
        }
        proveedorDAO.activar(idProveedor);
        return RespuestaOperacion.ok();
    }

    @Override
    public RespuestaOperacion<Void> desactivar(int idProveedor) {
        if (idProveedor <= 0) {
            return RespuestaOperacion.error("El proveedor es inválido");
        }
        Proveedor proveedor = proveedorDAO.buscarPorId(idProveedor);
        if (proveedor == null) {
            return RespuestaOperacion.error("El proveedor no existe");
        }
        if (!proveedor.isActivo()) {
            return RespuestaOperacion.error("El proveedor ya se encuentra desactivado");
        }
        proveedorDAO.desactivar(idProveedor);
        return RespuestaOperacion.ok();
    }

    @Override
    public List<Proveedor> listarActivos() {
        return proveedorDAO.listarActivos();
    }

    private RespuestaOperacion<Void> validarComun(Proveedor p) {
        if (p == null) {
            return RespuestaOperacion.error("El proveedor es obligatorio");
        }

        if (p.getRazonSocial() == null) {
            return RespuestaOperacion.error("La razón social es obligatoria");
        }
        p.setRazonSocial(p.getRazonSocial().trim());
        if (p.getRazonSocial().isEmpty()) {
            return RespuestaOperacion.error("La razón social es obligatoria");
        }

        if (p.getRuc() == null) {
            return RespuestaOperacion.error("El RUC es obligatorio");
        }
        p.setRuc(p.getRuc().trim());
        if (!p.getRuc().matches("\\d{11}")) {
            return RespuestaOperacion.error("El RUC debe tener exactamente 11 dígitos numéricos");
        }

        if (p.getDireccion() != null) {
            p.setDireccion(p.getDireccion().trim());
        }
        if (p.getTelefono() != null) {
            p.setTelefono(p.getTelefono().trim());
        }
        if (p.getContacto() != null) {
            p.setContacto(p.getContacto().trim());
        }

        return RespuestaOperacion.ok();
    }

    @Override
    public List<Proveedor> listar() {
        return proveedorDAO.listar();
    }

    @Override
    public Proveedor buscarPorId(int idProveedor) {
        return proveedorDAO.buscarPorId(idProveedor);
    }

    @Override
    public Proveedor buscarPorRuc(String ruc) {
        return proveedorDAO.buscarPorRuc(ruc);
    }
}
