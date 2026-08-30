
package com.ferronor.sic.contabilidad.logica;

import com.ferronor.sic.contabilidad.modelo.dto.BalanceGeneralDTO;
import java.time.LocalDate;

public interface BalanceGeneralService {

    BalanceGeneralDTO obtenerBalanceGeneral(LocalDate fechaCorte);
}
