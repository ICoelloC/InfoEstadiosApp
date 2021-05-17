package com.icoello.myapplication.UI.perfil

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Gallery
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
import com.google.firebase.storage.FirebaseStorage
import com.icoello.myapplication.R
import com.icoello.myapplication.Utilidades.CirculoTransformacion
import com.icoello.myapplication.Utilidades.UtilEncryptor
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil.*

class PerfilFragment() : Fragment() {

    val REQUEST_CODE = 200
    val photoURIUser: String = ""

    private lateinit var perfilFoto: ImageView
    private lateinit var txtNombreUsuario: TextView
    private lateinit var txtEmail: TextView
    private lateinit var github: ImageView
    private lateinit var twitter: ImageView

    private lateinit var perfilViewModel: PerfilViewModel

    private lateinit var Auth: FirebaseAuth

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
        github = root.findViewById(R.id.perfilGitHub)
        twitter = root.findViewById(R.id.perfilTwitter)

        inicializar(user)

        twitter.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/ICoelloC"))
            startActivity(intent)
        }

        github.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ICoelloC"))
            startActivity(intent)
        }

        return root
    }

    private fun inicializar(user: FirebaseUser) {
        cargarDatos(user)
    }

    private fun cargarDatos(user: FirebaseUser) {

        txtNombreUsuario.text = user.displayName
        txtEmail.text = user.email

        Picasso.get()
            .load(Auth.currentUser?.photoUrl)
            .transform(CirculoTransformacion())
            .resize(130, 130)
            .into(perfilFoto)


    }
}
