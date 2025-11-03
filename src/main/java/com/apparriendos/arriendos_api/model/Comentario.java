/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.apparriendos.arriendos_api.model;

/**
 *
 * @author Administrador PC
 */
public class Comentario {
    
    private Long idComentario;

    //@NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    //@Positive(message = "El número debe ser positivo")
    private Integer numero;

    private String direccion;
    private Double metrosCuadrados;
    private String tipologia;

}
