/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.shared;

import java.util.List;

/**
 *
 * @author JEFERSON
 */
public interface IGeneralDAO<T, ID> {
    void insertar(T entidad);
    void actualizar(T entidad);
    T buscarPorId(ID id);
    List<T> listar();
}