package com.icoello.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.icoello.myapplication.Entidades.Usuario

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginLoginButton: Button
    private lateinit var loginGoogleLoginButton: Button
    private lateinit var loginGoRegisterButton: Button

    private lateinit var Auth: FirebaseAuth
    private lateinit var  FireStore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private val Rc_SIGN_IN = 9001

    private  val googleSignIn = 300

    companion object{
        private const val TAG = "Login"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Auth = Firebase.auth
        FireStore = FirebaseFirestore.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        initUI()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Rc_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e: ApiException){
                Toast.makeText(baseContext, "Error: " + e.localizedMessage,
                    Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    val user = Auth.currentUser
                    Log.i(TAG, user.toString())
                    Toast.makeText(baseContext, "Auth: Usuario autenticado en Google", Toast.LENGTH_LONG).show()
                    user?.let { insertarUsuario(it) }
                    abrirMain()
                }else{
                    Log.w(TAG, "signInWithCredential: Error", task.exception)
                    Toast.makeText(baseContext, "Error: " + task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun insertarUsuario(user: FirebaseUser) {
        val usuario = Usuario(
            id = user.uid,
            username = user.displayName.toString(),
            correo = user.email.toString(),
            foto = user.photoUrl.toString()
        )
        FireStore.collection("usuarios")
            .document(usuario.id)
            .set(usuario)
            .addOnSuccessListener { Log.i(TAG, "Usuario insertado") }
            .addOnFailureListener { e -> Log.w(TAG, "Error insertar usuario", e) }
    }

    private fun initUI() {
        loginEmail = findViewById(R.id.loginEmail)
        loginPassword = findViewById(R.id.loginPassword)
        loginLoginButton = findViewById(R.id.loginLoginButton)
        loginGoogleLoginButton = findViewById(R.id.loginGoogleLoginButton)
        loginGoRegisterButton = findViewById(R.id.loginGoRegisterButton)

        loginLoginButton.setOnClickListener {
            val email = loginEmail.text.toString()
            val password = loginPassword.text.toString()
            if (checkEmpty(email, password)) {
                iniciarSesion(email, password)
            }
        }

        loginGoogleLoginButton.setOnClickListener { iniciarSesionGoogle() }

        loginGoRegisterButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun iniciarSesionGoogle() {
        /*
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, Rc_SIGN_IN)
         */
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(this, googleConf)
        googleClient.signOut()

        startActivityForResult(googleClient.signInIntent, googleSignIn)
    }


    private fun iniciarSesion(email: String, password: String) {
        Auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    abrirMain()
                } else {
                    showErrorAlert("Error al iniciar sesión, no hay ningún usuario con ese correo o contraseña")
                }
            }
    }

    private fun abrirMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showErrorAlert(message:String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun checkEmpty(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }
}