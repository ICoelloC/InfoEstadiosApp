package com.icoello.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE

class RegisterActivity : AppCompatActivity() {
    private lateinit var registerUsername: EditText
    private lateinit var registerEmail: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var registerGoLoginButton: Button
    private lateinit var imgFoto: ImageView

    private lateinit var auth: FirebaseAuth

    val REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        registerUsername = findViewById(R.id.registerUsername)
        registerEmail = findViewById(R.id.registerEmail)
        registerPassword = findViewById(R.id.registerPassword)
        registerButton = findViewById(R.id.registerBtnRegister)
        registerGoLoginButton = findViewById(R.id.registerGoLoginButton)
        imgFoto = findViewById(R.id.registerFoto)

        registerButton.setOnClickListener {
            registrarseNormal()
        }

        registerGoLoginButton.setOnClickListener {
            startActivity((Intent(this, LoginActivity::class.java)))
            finish()
        }

        imgFoto.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE)
            }

            if (checkSelfPermission(android.Manifest.permission.CAMERA)  == PackageManager.PERMISSION_GRANTED) {
                foto()
            }else{
                Toast.makeText (this, "Camera permission necesary", Toast.LENGTH_SHORT).show()
            }
        }

        capturePhoto()

    }

    private fun foto() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE)
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE)
    }

    private fun showErrorAlert(message:String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun capturePhoto() {
        imgFoto.setOnClickListener {

            if (checkSelfPermission(android.Manifest.permission.CAMERA)  != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE)
            }

            if (checkSelfPermission(android.Manifest.permission.CAMERA)  == PackageManager.PERMISSION_GRANTED) {
                foto()
            }else{
                Toast.makeText (this, "Camera permission necesary", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun registrarseNormal() {

        var email = registerEmail.text.toString()
        var password = registerPassword.text.toString()
        var username = registerUsername.text.toString()

        Log.i("REGISTRAR", username)
        Log.i("REGISTRAR", email)
        Log.i("REGISTRAR", password)

        if (checkEmpty(email, password, username)) {
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

    private fun checkEmpty(email: String, password: String, username: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()
    }
}