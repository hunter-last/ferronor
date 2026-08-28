package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.dto.EstadoResultadosDTO;
import java.time.LocalDate;

public interface EstadoResultadosService {

    EstadoResultadosDTO obtenerEstadoResultados(LocalDate hasta);

    EstadoResultadosDTO obtenerEstadoResultados(
            LocalDate desde,
            LocalDate hasta
    );
}
