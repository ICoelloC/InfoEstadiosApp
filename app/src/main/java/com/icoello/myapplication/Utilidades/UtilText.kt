package com.icoello.myapplication.Utilidades

import android.content.Context
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import com.icoello.myapplication.R

object UtilText {

    fun cleanErrors(vararg errors: EditText) {
        for (error in errors) error.error = null
    }

    fun isMailValid(mail: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(mail).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    fun empty(txt: TextView, txtLay: TextInputLayout, context: Context): Boolean {
        var empty = false
        if (txt.text.isEmpty()) {
            txtLay.error = context.resources.getString(R.string.isEmpty)
            empty = true
        }
        return empty
    }

    fun checkEmpty(input: String): Boolean {
        return input.isNotEmpty()
    }

}