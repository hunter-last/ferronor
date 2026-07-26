/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.shared;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class SesionUsuario {

    private static SesionUsuario instancia;

    private final int idUsuario;
    private final String nombreCompleto;
    private final String nombreRol;
    private final Set<String> permisos;

    private SesionUsuario(int idUsuario, String nombreCompleto, String nombreRol, Set<String> permisos) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.nombreRol = nombreRol;
        this.permisos = Collections.unmodifiableSet(new HashSet<>(permisos));
    }

    public static void iniciar(int idUsuario, String nombreCompleto, String nombreRol, Set<String> permisos) {
        instancia = new SesionUsuario(idUsuario, nombreCompleto, nombreRol, permisos);
    }

    public static boolean haySesion() {
        return instancia != null;
    }

    public static boolean puedeAcceder(String permiso) {
        return haySesion() && actual().tienePermiso(permiso);
    }

    public static Optional<SesionUsuario> obtener() {
        return Optional.ofNullable(instancia);
    }

    public static SesionUsuario actual() {
        if (instancia == null) {
            throw new IllegalStateException("No hay una sesión de usuario activa");
        }
        return instancia;
    }

    public static void cerrar() {
        instancia = null;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public boolean tienePermiso(String codigo) {
        return permisos.contains(codigo);
    }

    public Set<String> getPermisos() {
        return permisos;
    }
}
