
package com.ferronor.sic.inventario.logica;

import com.ferronor.sic.inventario.modelo.AjusteInventario;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.time.LocalDate;
import java.util.List;

public interface AjusteInventarioService {

    RespuestaOperacion<Void> registrarAjuste(int idProducto, java.math.BigDecimal cantidadFisica,
            String motivo, int idUsuario);

    List<AjusteInventario> listarAjustes(LocalDate desde, LocalDate hasta);
}