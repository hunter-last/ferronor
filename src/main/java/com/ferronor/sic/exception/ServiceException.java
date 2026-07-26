/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.exception;

public class ServiceException extends RuntimeException {

    public ServiceException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ServiceException(String mensaje) {
        super(mensaje);
    }
}
