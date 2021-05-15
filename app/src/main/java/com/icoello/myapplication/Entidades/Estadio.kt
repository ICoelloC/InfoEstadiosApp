package com.icoello.myapplication.Entidades

import java.util.*

data class Estadio(
    var id: String = "",
    var nombre: String = "",
    var capacidad: Int = 0,
    var equipo: String = "",
    var latitud: String = "",
    var longitud: String = "",
    var seguidores: Int = 0,
    var id_usuario: String = "",
    var  foto:String = "",
    var seguido: Boolean = false,
) {
    constructor(
        nombre: String,
        capacidad: Int,
        equipo: String,
        latitud: String,
        longitud: String,
        seguidores: Int,
        id_usuario: String,
        foto: String,
        seguido: Boolean,
    ) : this(
        (UUID.randomUUID().toString()),
        nombre,
        capacidad,
        equipo,
        latitud,
        longitud,
        seguidores,
        id_usuario,
        foto,
        seguido
    )

    override fun toString(): String {
        return "Lugar(id=$id, nombre=$nombre, capacidad=$capacidad, equipo=$equipo, latitud=$latitud, longitud=$longitud, seguidores=$seguidores, id_usuario = $id_usuario, foto =$foto, seguido=$seguido)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Estadio) return false

        if (id != other.id) return  false
        if (nombre != other.nombre) return  false
        if (capacidad != other.capacidad) return  false
        if (equipo != other.equipo) return  false
        if (latitud != other.latitud) return  false
        if (longitud != other.longitud) return  false
        if (foto != other.foto) return  false
        if (seguido != other.seguido) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + nombre.hashCode()
        result = 31 * result + capacidad.hashCode()
        result = 31 * result + equipo.hashCode()
        result = 31 * result + latitud.hashCode()
        result = 31 * result + longitud.hashCode()
        result = 31 * result + seguidores.hashCode()
        result = 31 * result + id_usuario.hashCode()
        result = 31 * result + foto.hashCode()
        result = 31 * result + seguido.hashCode()
        return result
    }
}
