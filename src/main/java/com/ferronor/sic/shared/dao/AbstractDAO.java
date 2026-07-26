/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.shared.dao;

import com.ferronor.sic.conexion.ConexionPostgres;
import com.ferronor.sic.conexion.TransactionManager;
import com.ferronor.sic.exception.DaoException;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDAO {

    protected Connection obtenerConexion() {
        Connection cn = TransactionManager.obtenerActual();
        return (cn != null) ? cn : ConexionPostgres.obtener();
    }

    protected boolean esConexionTransaccional() {
        return TransactionManager.obtenerActual() != null;
    }

    protected void cerrar(Connection cn) {
        if (!esConexionTransaccional()) {
            try {
                if (cn != null) {
                    cn.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    protected DaoException error(String mensaje, SQLException e) {
        return new DaoException(mensaje, e);
    }
}
