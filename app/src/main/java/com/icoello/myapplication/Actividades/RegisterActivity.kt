package com.icoello.myapplication.Actividades

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.icoello.myapplication.App.MyApp
import com.icoello.myapplication.R
import com.icoello.myapplication.Utilidades.Fotos
import com.icoello.myapplication.Utilidades.UtilEncryptor
import com.icoello.myapplication.Utilidades.UtilText
import com.icoello.myapplication.Utilidades.Utils
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private var name = ""
    private var nameuser = ""
    private var email = ""
    private var pass = ""
    private lateinit var FOTO: Bitmap
    private var IMAGE: Uri? = null
    private var image: Bitmap? = null

    private lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage

    private lateinit var IMAGEN_NOMBRE: String
    private lateinit var txtUsername: String
    private lateinit var txtPassword: String
    private lateinit var txtEmail: String

    val REQUEST_CODE = 200

    private val IMAGEN_DIR = "/InfoEstadios"
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initUI()
        txtUsername = registerUsername.text.toString().trim()
        txtPassword = UtilEncryptor.encrypt(registerPassword.text.toString().trim())!!
        txtEmail = registerEmail.text.toString().trim()

    }

    fun signIn() {
        registerBtnRegister.setOnClickListener {
            UtilText.cleanErrors(registerEmail, registerUsername, registerPassword)
            createAccount()
        }
    }

    fun startLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun anyEmpty(): Boolean {
        var valid = true
        if (UtilText.checkEmpty(txtEmail) || UtilText.checkEmpty(txtUsername)
            || UtilText.checkEmpty(txtPassword)
        ) {
            valid = false
        }
        return valid
    }

    private fun initUI() {
        storage = Firebase.storage
        auth = Firebase.auth
        initButtoms()
        signIn()
    }

    private fun initButtoms() {
        registerFoto.setOnClickListener {
            initDialogFoto()
        }
        registerGoLoginButton.setOnClickListener {
            abrirLogin()
        }
    }

    private fun abrirLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun initDialogFoto() {
        val fotoDialogoItems = arrayOf(
            getString(R.string.Gallery),
            getString(R.string.Photo)
        )
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.SelectOption))
            .setItems(fotoDialogoItems) { _, modo ->
                when (modo) {
                    0 -> {
                        if ((this.application as MyApp).initPermissesGallery()) {
                            takephotoFromGallery()
                        } else {
                            (this.application as MyApp).initPermissesGallery()
                        }
                    }
                    1 -> {
                        if ((this.application as MyApp).initPermissesCamera()) {
                            takePhotoFromCamera()
                        } else {
                            (this.application as MyApp).initPermissesCamera()
                        }
                    }
                }
            }
            .show()
    }

    private fun takephotoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Nombre de la imagen
        IMAGEN_NOMBRE = Fotos.crearNombreFichero()
        // guardamos el fichero en una variable
        val file = Fotos.salvarImagen(IMAGEN_DIR, IMAGEN_NOMBRE, this)
        IMAGE = Uri.fromFile(file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGE)
        startActivityForResult(intent, CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Si cancela no hace nada
        if (resultCode == RESULT_CANCELED) {
            Log.d("sing", "Se ha cancelado")
        }
        //si elige la opcion de galeria entra en la galeria
        if (requestCode == GALLERY) {
            Log.d("sing", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI
                val contentURI = data.data!!
                try {
                    FOTO = differentVersion(contentURI)
                    FOTO = Fotos.scaleImage(FOTO, 800, 800)
                    registerFoto.setImageBitmap(FOTO)//mostramos la imagen
                    Fotos.redondearFoto(registerFoto)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, getText(R.string.error_gallery), Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMERA) {
            Log.d("sing", "Entramos en Camara")
            //cogemos la imagen
            try {
                FOTO = differentVersion(IMAGE!!)
                FOTO = Fotos.scaleImage(FOTO, 800, 800)
                // Mostramos la imagen
                registerFoto.setImageBitmap(FOTO)
                Fotos.redondearFoto(registerFoto)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (ex: Exception) {
                Toast.makeText(this, getText(R.string.error_camera), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun differentVersion(contentURI: Uri): Bitmap {
        val bitmap: Bitmap
        bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(contentResolver, contentURI);
        } else {
            val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, contentURI)
            ImageDecoder.decodeBitmap(source)
        }
        return bitmap;
    }

    private fun isCorrect(txtemail: String): Boolean {
        var valide = false
        if (Utils.isNetworkAvailable(this)) {
            valide = true
        } else {
            val snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                R.string.no_net,
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setActionTextColor(getColor(R.color.accent))
            snackbar.setAction("Conectar") {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
                finish()
            }
            snackbar.show()
        }
        return valide
    }

    private fun createAccount() {
        txtPassword = UtilEncryptor.encrypt(registerPassword.text.toString().trim())!!
        txtEmail = registerEmail.text.toString().trim()
        if (!isCorrect(txtEmail)) {
            return
        }
        Log.d("Firebase", "createAccount:$txtEmail")
        auth.createUserWithEmailAndPassword(txtEmail, txtPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("fairbase", "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateProfile(user!!)
                    startLogin()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("fairbase", "createUserWithEmail:failure", task.exception)
                    registerEmail.error = resources.getString(R.string.isAlreadyExist)
                }
            }
    }

    private fun updateProfile(user: FirebaseUser) {
        val profileUpdates = userProfileChangeRequest {
            displayName = registerUsername.text.toString()
            loadImage(displayName!!, user)
        }
        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "User profile updated.")
                }
            }
    }

    private fun loadImage(string: String, user: FirebaseUser) {
        if (!this::FOTO.isInitialized) {
            return
        }
        val baos = ByteArrayOutputStream()
        FOTO.compress(Bitmap.CompressFormat.JPEG, 40, baos)
        val data = baos.toByteArray()
        val imageRef = storage.reference.child("images/users/${auth.uid}.jpg")
        var uploadTask = imageRef.putBytes(data)
        //descarga y referencia URl
        uploadTask.addOnFailureListener {
            Log.i("fairebase", "error al subir la foto a storage")
        }.addOnSuccessListener { taskSnapshot ->
            val dowuri = taskSnapshot.metadata!!.reference!!.downloadUrl
            dowuri.addOnSuccessListener { task ->
                val profileUpdates = userProfileChangeRequest {
                    photoUri = task
                    Log.i("fairebase", "uri: $task")
                }
                //modifica con los cambios de la uri
                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("TAG", "uri profile good")
                        }
                    }
            }
        }

    }


    private fun foto() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CODE
            )
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, REQUEST_CODE)
    }

    private fun showErrorAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun capturePhoto() {
        registerFoto.setOnClickListener {

            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    REQUEST_CODE
                )
            }

            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                foto()
            } else {
                Toast.makeText(this, "Camera permission necesary", Toast.LENGTH_SHORT).show()
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