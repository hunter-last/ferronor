package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.modelo.PlanCuenta;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface PlanCuentaService {

    RespuestaOperacion<Void> registrar(PlanCuenta cuenta);

    RespuestaOperacion<Void> actualizar(PlanCuenta cuenta);

    List<PlanCuenta> listar();

    List<PlanCuenta> listarHijos(int idCuentaPadre);

    List<PlanCuenta> listarRaiz();

    PlanCuenta buscarPorId(int idCuenta);

    PlanCuenta buscarPorCodigo(String codigo);
}
