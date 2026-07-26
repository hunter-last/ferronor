package com.ferronor.sic.conexion;

import com.ferronor.sic.config.Configuracion;
import com.ferronor.sic.exception.DaoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionPostgres {

    private ConexionPostgres() {
    }

    public static Connection obtener() {
        try {
            return DriverManager.getConnection(Configuracion.dbUrl(), Configuracion.dbUsuario(), Configuracion.dbPassword());
        } catch (SQLException e) {
            throw new DaoException("No se pudo conectar a la base de datos", e);
        }
    }
}
