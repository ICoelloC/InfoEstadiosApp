package com.icoello.myapplication.Actividades

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.icoello.myapplication.Entidades.Usuario
import com.icoello.myapplication.R
import com.icoello.myapplication.Utilidades.UtilEncryptor
import com.icoello.myapplication.Utilidades.Utils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var FireStore: FirebaseFirestore

    private val GOOGLE_SIGN_IN = 333

    var email: String = ""
    var pass: String = ""

    enum class ProviderType {
        BASIC,
        GOOGLE
    }

    companion object {
        private const val TAG = "Login"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        initGoogle()
        loginLoginButton.setOnClickListener {
            login()
        }
        loginGoogleLoginButton.setOnClickListener {
            loginGoogle()
        }
        loginGoRegisterButton.setOnClickListener {
            val googleConf =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

    }

    private fun initGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
    }

    private fun loginGoogle() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account = task.getResult(ApiException::class.java)
                if (account != null) {

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                abrirMain()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Error al iniciar sesion con Google",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                }

            } catch (e: ApiException) {
                Toast.makeText(this, "Error al iniciar sesion con Google", Toast.LENGTH_SHORT)
                    .show()
            }

        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FIREBASE", "signInWithCredential:success")
                    val user = auth.currentUser
                    user?.let { insertarUsuario(it) }
                    abrirMain()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FIREBASE", "signInWithCredential:failure", task.exception)
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
            .addOnSuccessListener { Log.i(TAG, "Usuario insertado!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error insertar usuario", e) }
    }

    private fun login() {
        email = loginEmail.text.toString().trim()
        pass = UtilEncryptor.encrypt(loginPassword.text.toString().trim())!!

        if (checkEmpty(email, pass)) {
            if (Utils.isNetworkAvailable(this)) {
                userExists(email, pass)
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
            Log.i("realm", "usuario logeado")
        }
    }

    private fun userExists(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.i("fairbase", "signInWithEmail:success")
                    val user = auth.currentUser
                    Log.i("fairbase", user.toString())
                    abrirMain()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("fairbase", "signInWithEmail:failure", task.exception)
                    loginEmail.error = resources.getString(R.string.userNotCorrect)
                }

            }
    }

    private fun abrirMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showErrorAlert(message: String) {
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