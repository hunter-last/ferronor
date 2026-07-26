/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.exception;

/**
 *
 * @author JEFERSON
 */
public class DaoException extends RuntimeException {

    public DaoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public DaoException(String mensaje) {
        super(mensaje);
    }
}
