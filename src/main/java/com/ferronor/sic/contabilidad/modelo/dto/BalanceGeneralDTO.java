
package com.ferronor.sic.contabilidad.modelo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BalanceGeneralDTO {

    private final LocalDate fechaCorte;
    private final List<BalanceGeneralItem> activo = new ArrayList<>();
    private final List<BalanceGeneralItem> pasivo = new ArrayList<>();
    private final List<BalanceGeneralItem> patrimonio = new ArrayList<>();
    private BigDecimal totalActivo = BigDecimal.ZERO;
    private BigDecimal totalPasivo = BigDecimal.ZERO;
    private BigDecimal totalPatrimonio = BigDecimal.ZERO;

    public BalanceGeneralDTO(LocalDate fechaCorte) {
        this.fechaCorte = fechaCorte;
    }

    public void agregarActivo(BalanceGeneralItem item) {
        activo.add(item);
        totalActivo = totalActivo.add(item.getSaldo());
    }

    public void agregarPasivo(BalanceGeneralItem item) {
        pasivo.add(item);
        totalPasivo = totalPasivo.add(item.getSaldo());
    }

    public void agregarPatrimonio(BalanceGeneralItem item) {
        patrimonio.add(item);
        totalPatrimonio = totalPatrimonio.add(item.getSaldo());
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public List<BalanceGeneralItem> getActivo() {
        return activo;
    }

    public List<BalanceGeneralItem> getPasivo() {
        return pasivo;
    }

    public List<BalanceGeneralItem> getPatrimonio() {
        return patrimonio;
    }

    public BigDecimal getTotalActivo() {
        return totalActivo;
    }

    public BigDecimal getTotalPasivo() {
        return totalPasivo;
    }

    public BigDecimal getTotalPatrimonio() {
        return totalPatrimonio;
    }
}
