
package com.ferronor.sic.shared;

public class RespuestaOperacion<T> {

    private final boolean exito;
    private final String mensaje;
    private final T resultado;

    private RespuestaOperacion(boolean exito, String mensaje, T resultado) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.resultado = resultado;
    }

    public static <T> RespuestaOperacion<T> ok() {
        return new RespuestaOperacion<>(true, null, null);
    }

    public static <T> RespuestaOperacion<T> ok(T resultado) {
        return new RespuestaOperacion<>(true, null, resultado);
    }

    public static <T> RespuestaOperacion<T> error(String mensaje) {
        return new RespuestaOperacion<>(false, mensaje, null);
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public T getResultado() {
        return resultado;
    }
}
