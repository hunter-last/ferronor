/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.contabilidad.dao;

import com.ferronor.sic.contabilidad.modelo.dto.BalanceComprobacionItem;
import com.ferronor.sic.shared.dao.AbstractDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BalanceComprobacionDAOImpl extends AbstractDAO implements BalanceComprobacionDAO {

    // BalanceComprobacionDAOImpl — comentario agregado sobre el JOIN
// NOTA: usamos JOIN (no LEFT JOIN) intencionalmente — el Balance de Comprobación
// de Ferronor solo necesita mostrar cuentas con movimientos reales. Si en el futuro
// se requiere ver el catálogo completo (incluyendo cuentas sin movimiento), cambiar
// a LEFT JOIN detalle_asiento y mover las condiciones de asiento_contable al ON
// del JOIN correspondiente, para no convertir el LEFT JOIN en INNER JOIN implícito.
    @Override
    public List<BalanceComprobacionItem> obtenerAgregadoPorCuenta(LocalDate hasta) {
        String sql = "SELECT pc.id_cuenta, pc.codigo, pc.nombre_cuenta, "
                + "COALESCE(SUM(da.debe),0) AS total_debe, COALESCE(SUM(da.haber),0) AS total_haber "
                + "FROM plan_cuenta pc "
                + "JOIN detalle_asiento da ON da.id_cuenta = pc.id_cuenta "
                + "JOIN asiento_contable a ON da.id_asiento = a.id_asiento "
                + "WHERE a.estado = 'ACTIVO' AND a.fecha < ? "
                + "GROUP BY pc.id_cuenta, pc.codigo, pc.nombre_cuenta "
                + "ORDER BY pc.codigo";
        Connection cn = obtenerConexion();
        List<BalanceComprobacionItem> resultado = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new BalanceComprobacionItem(
                            rs.getInt("id_cuenta"), rs.getString("codigo"), rs.getString("nombre_cuenta"),
                            rs.getBigDecimal("total_debe"), rs.getBigDecimal("total_haber")));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw error("Error al obtener el balance de comprobación", e);
        } finally {
            cerrar(cn);
        }
    }
}
