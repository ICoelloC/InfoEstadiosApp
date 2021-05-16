package com.icoello.myapplication.Utilidades

import java.lang.Exception
import java.security.MessageDigest
import kotlin.experimental.and

object UtilEncryptor {

    fun encrypt(pwd: String): String? {
        var md: MessageDigest?
        var bytes: ByteArray? = null
        try {
            md = MessageDigest.getInstance("SHA-256")
            bytes = md.digest(pwd.toByteArray(charset("UTF-8")))
        } catch (ex: Exception) {
        }
        return convertToHex(bytes)
    }

    fun convertToHex(bytes: ByteArray?): String? {
        val sb = StringBuffer()
        for (i in bytes!!.indices) {
            sb.append(((bytes[i] and 0xff.toByte()) + 0x100).toString(16).substring(1))
        }
        return sb.toString()
    }

}