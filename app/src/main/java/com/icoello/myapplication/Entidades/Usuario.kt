package com.icoello.myapplication.Entidades

import java.util.*

data class Usuario(
    var id: String = "",
    var username: String = "",
    var correo: String = "",
    var password: String = "",
    var foto: String = "",
) {
    constructor(
        username: String,
        correo: String,
        password: String,
        foto: String,
    ) : this((UUID.randomUUID().toString()), username, correo, password, foto)

    override fun toString(): String {
        return "Usuario(id=$id, nombre=$username, correo=$correo, password=$password, foto=$foto)"
    }
}
