package com.icoello.myapplication.UI.perfil

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.icoello.myapplication.R

class PerfilFragment() : Fragment() {

    val REQUEST_CODE = 200
    val photoURIUser: String =""

    private lateinit var perfilFoto: ImageView
    private lateinit var txtNombreUsuario: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtPassword: TextView
    private lateinit var btnEditar: FloatingActionButton

    private lateinit var perfilViewModel: PerfilViewModel

    private lateinit var Auth: FirebaseAuth
    private lateinit var FireStore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Auth = Firebase.auth

        var user = Auth.currentUser

        val root = inflater.inflate(R.layout.fragment_perfil, container, false)

        perfilFoto = root.findViewById(R.id.perfilFoto)
        txtNombreUsuario = root.findViewById(R.id.perfilUsername)
        txtEmail = root.findViewById(R.id.perfilEmail)
        txtPassword = root.findViewById(R.id.perfilPassword)
        btnEditar = root.findViewById(R.id.perfilEditarButton)

        inicializar(user)

        btnEditar.setOnClickListener {
            dialogUpdate(user)
        }

        return root
    }
/*
    fun changePhoto(){
        var auth = FirebaseAuth.getInstance()
        auth.currentUser?.let { user ->
            var photoURI = Uri.parse(photoURIUser)
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(photoURI)
                .build()

            user.updateProfile(profileUpdates)

        }
        showSucced()
    }

    fun changeCredentials(){
        btnChange.setOnClickListener() {
            val prefs = this.requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            val provider = prefs.getString("provider", null)

            if (provider.equals(ProviderType.BASIC.toString())) {
                val auth = FirebaseAuth.getInstance()
                if (txtPassword.text.toString().trim() != "")
                    changeUserPassword(auth)
                FireStore.collection("usuarios").document(getString(R.string.prefs_file)).set(
                    hashMapOf(
                        "username" to txtNombreUsuario.toString(),
                        "address" to prefs.getString("email", null),
                        "provider" to ProviderType.BASIC.toString()
                    )
                )
                showSucced()
                if (txtNameChange.text.toString().trim() != "")
                    changeUserName(auth)
                showSucced()


            }else {
                showAlert()
            }
        }
    }
*/
    private fun changeUserName(auth: FirebaseAuth) {
        auth.currentUser?.let { user ->
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(txtNombreUsuario.text.toString().trim())
                .build()
            user.updateProfile(profileUpdates)
        }
    }

    private fun changeUserPassword(auth: FirebaseAuth) {
        auth.currentUser.updatePassword(txtPassword.text.toString().trim())
    }


    fun changePhoto(){
        var auth = FirebaseAuth.getInstance()
        auth.currentUser?.let { user ->
            var photoURI = Uri.parse(photoURIUser)
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setPhotoUri(photoURI)
                .build()

            user.updateProfile(profileUpdates)

        }
        //showSucced()
    }

    private fun dialogUpdate(user: FirebaseUser) {
        AlertDialog.Builder(context).setTitle("Aviso")
            .setMessage("Seguro de que quieres modificar tu cuenta")
            .setPositiveButton("OK") { _, _ ->
                Log.i("updater", "usuario cambia")
                update(user)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun update(user: FirebaseUser) {
        var email = user.email
        var username = ""
        var password = ""
        if (txtNombreUsuario.text.isNotEmpty()) {
            username = txtNombreUsuario.text.toString()
        }
        if (txtEmail.text.isNotEmpty()) {
            email = txtEmail.text.isNotEmpty().toString()
        }
        if (txtPassword.text.isNotEmpty()) {
            password = txtPassword.toString()
        }
    }

    private fun inicializar(user: FirebaseUser) {
        cargarDatos(user)
    }

    private fun cargarDatos(user: FirebaseUser) {

        txtNombreUsuario.text = user.displayName
        txtEmail.text = user.email

    }
}