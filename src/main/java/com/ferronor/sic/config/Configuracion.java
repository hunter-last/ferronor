/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Configuracion {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = Configuracion.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró config.properties en el classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar config.properties", e);
        }
    }

    private Configuracion() {
    }

    private static String requerida(String clave) {
        String valor = props.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la propiedad obligatoria: " + clave);
        }
        return valor;
    }

    public static String dbUrl() {
        return requerida("db.url");
    }

    public static String dbUsuario() {
        return requerida("db.usuario");
    }

    public static String dbPassword() {
        return requerida("db.password");
    }
}
