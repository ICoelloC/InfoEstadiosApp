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
) {
    constructor(
        nombre: String,
        capacidad: Int,
        equipo: String,
        latitud: String,
        longitud: String,
        seguidores: Int
    ) : this(
        (UUID.randomUUID().toString()),
        nombre,
        capacidad,
        equipo,
        latitud,
        longitud,
        seguidores
    )

    override fun toString(): String {
        return "Lugar(id=$id, nombre=$nombre, capacidad=$capacidad, equipo=$equipo, latitud=$latitud, longitud=$longitud, seguidores)"
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
        return result
    }
}
