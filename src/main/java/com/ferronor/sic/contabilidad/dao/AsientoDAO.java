package com.ferronor.sic.contabilidad.dao;

import com.ferronor.sic.contabilidad.modelo.AsientoContable;
import java.time.LocalDate;
import java.util.List;

public interface AsientoDAO {

    void insertar(AsientoContable asiento);

    void anular(int idAsiento);

    AsientoContable buscarPorId(int idAsiento);

    List<AsientoContable> listarPorRangoFecha(LocalDate desde, LocalDate hasta);

    List<AsientoContable> listar();
}
