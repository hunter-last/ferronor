package com.ferronor.sic.shared;

import javax.swing.*;

public abstract class FrmBase extends JFrame {

    protected FrmBase(String permisoModulo) {
        validarAcceso(permisoModulo);
    }

    private void validarAcceso(String permisoModulo) {
        if (!SesionUsuario.haySesion()) {
            throw new IllegalStateException("No existe una sesión activa.");
        }
        if (permisoModulo != null && !SesionUsuario.actual().tienePermiso(permisoModulo)) {
            throw new SecurityException("No tiene permisos para acceder a este módulo.");
        }
    }
}
