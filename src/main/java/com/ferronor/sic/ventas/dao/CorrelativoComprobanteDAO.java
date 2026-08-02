package com.ferronor.sic.ventas.dao;

// No extiende IGeneralDAO/IHistoricoDAO: correlativo_comprobante no es una entidad de
// negocio con ciclo de vida propio, es un contador atómico pre-sembrado por
// tipo_comprobante (ver 10_datos_iniciales.sql). Un único método atómico basta.
public interface CorrelativoComprobanteDAO {

    // UPDATE correlativo_comprobante SET ultimo_numero = ultimo_numero + 1
    // WHERE id_tipo_comprobante = ? RETURNING ultimo_numero — nunca SELECT + Java + UPDATE.
    int obtenerSiguienteNumero(int idTipoComprobante);
}