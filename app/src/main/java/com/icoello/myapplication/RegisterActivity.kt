package com.icoello.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var registerEmail: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerRepeatPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var registerLoginButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        registerEmail = findViewById(R.id.registerEmail)
        registerPassword = findViewById(R.id.registerPassword)
        registerRepeatPassword = findViewById(R.id.registerRepitePassword)
        registerButton = findViewById(R.id.registerBtnRegister)
        registerLoginButton = findViewById(R.id.loginRegistrarseButton)

        registerButton.setOnClickListener {
            registrarseNormal()
        }

        if (registerLoginButton != null){
            registerLoginButton.setOnClickListener {
                startActivity((Intent(this, LoginActivity::class.java)))
                finish()
            }
        }



    }

    private fun registrarseNormal() {

        var email = registerEmail.text.toString()
        var password = registerPassword.text.toString()
        var repeatPassword = registerRepeatPassword.text.toString()

        if (password.equals(repeatPassword) && checkEmpty(email, password, repeatPassword)) {
            firebaseNormalRegister(email, password)
        }

    }

    private fun firebaseNormalRegister(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(applicationContext, "Register failed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkEmpty(email: String, password: String, repeatPassword: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()
    }
}