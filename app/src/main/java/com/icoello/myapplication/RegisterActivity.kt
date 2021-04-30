package com.icoello.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var registerEmail: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerRepeatPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var registerGoLoginButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        registerEmail = findViewById(R.id.registerEmail)
        registerPassword = findViewById(R.id.registerPassword)
        registerRepeatPassword = findViewById(R.id.registerRepitePassword)
        registerButton = findViewById(R.id.registerBtnRegister)
        registerGoLoginButton = findViewById(R.id.registerGoLoginButton)

        registerButton.setOnClickListener {
            registrarseNormal()
        }

        registerGoLoginButton.setOnClickListener {
            startActivity((Intent(this, LoginActivity::class.java)))
            finish()
        }

    }

    private fun showErrorAlert(message:String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun registrarseNormal() {

        var email = registerEmail.text.toString()
        var password = registerPassword.text.toString()
        var repeatPassword = registerRepeatPassword.text.toString()

        if (password == repeatPassword && checkEmpty(email, password, repeatPassword)) {
            firebaseNormalRegister(email, password)
        }

    }

    private fun firebaseNormalRegister(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                showErrorAlert("Error al registrarse, credenciales incorrectas o repetidas")
            }
        }
    }

    private fun checkEmpty(email: String, password: String, repeatPassword: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()
    }
}