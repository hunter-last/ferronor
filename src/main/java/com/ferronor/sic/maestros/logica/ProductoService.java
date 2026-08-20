package com.ferronor.sic.maestros.logica;

import com.ferronor.sic.maestros.modelo.Producto;
import com.ferronor.sic.shared.RespuestaOperacion;
import java.util.List;

public interface ProductoService {

    RespuestaOperacion<Void> registrar(Producto producto);

    RespuestaOperacion<Void> actualizar(Producto producto);

    RespuestaOperacion<Void> desactivar(int idProducto);

    RespuestaOperacion<Void> activar(int idProducto);

    List<Producto> listarActivos();

    Producto buscarPorId(int idProducto);

    Producto buscarPorCodigo(String codigo);

    List<Producto> buscarActivosPorNombreOCodigoParcial(String texto);

}
