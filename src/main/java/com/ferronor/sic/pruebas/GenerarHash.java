/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ferronor.sic.pruebas;

import org.mindrot.jbcrypt.BCrypt;

public class GenerarHash {

    public static void main(String[] args) {
        System.out.println(
            BCrypt.hashpw("1234567", BCrypt.gensalt())
        );
    }

}