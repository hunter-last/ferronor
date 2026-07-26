
package com.ferronor.sic.conexion;

import com.ferronor.sic.exception.DaoException;
import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionManager {

    private static final ThreadLocal<Connection> CONEXION_ACTIVA = new ThreadLocal<>();

    private TransactionManager() {
    }

    public static TransactionContext iniciar() {
        if (CONEXION_ACTIVA.get() != null) {
            return new TransactionContext(false); // ya existe una activa: nos unimos, no anidamos
        }
        try {
            Connection cn = ConexionPostgres.obtener();
            cn.setAutoCommit(false);
            CONEXION_ACTIVA.set(cn);
            return new TransactionContext(true);
        } catch (SQLException e) {
            throw new DaoException("No se pudo iniciar la transacción", e);
        }
    }

    public static Connection obtenerActual() {
        return CONEXION_ACTIVA.get();
    }

    static void confirmar(boolean esDuenio) {
        if (!esDuenio) {
            return;
        }
        Connection cn = CONEXION_ACTIVA.get();
        try {
            cn.commit();
        } catch (SQLException e) {
            throw new DaoException("Error al confirmar la transacción", e);
        } finally {
            cerrar(cn);
        }
    }

    static void revertir(boolean esDuenio) {
        if (!esDuenio) {
            return;
        }
        Connection cn = CONEXION_ACTIVA.get();
        try {
            if (cn != null) {
                cn.rollback();
            }
        } catch (SQLException e) {
            throw new DaoException("Error al revertir la transacción", e);
        } finally {
            cerrar(cn);
        }
    }

    private static void cerrar(Connection cn) {
        try {
            if (cn != null) {
                cn.close();
            }
        } catch (SQLException ignored) {
        } finally {
            CONEXION_ACTIVA.remove();
        }
    }
    
    
}
