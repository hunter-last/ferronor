/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.auditoria.dao;

import com.ferronor.sic.auditoria.modelo.Auditoria;
import java.util.List;

public interface AuditoriaDAO {
    void insertar(Auditoria auditoria);
    List<Auditoria> listar();
}