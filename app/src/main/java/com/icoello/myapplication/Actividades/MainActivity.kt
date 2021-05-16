package com.icoello.myapplication.Actividades

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.icoello.myapplication.App.MyApp
import com.icoello.myapplication.R
import com.icoello.myapplication.Utilidades.CirculoTransformacion
import com.icoello.myapplication.Utilidades.Utils
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var Auth: FirebaseAuth

    companion object {
        private const val TAG = "Main"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Auth = Firebase.auth

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_estadios, R.id.nav_mapa, R.id.nav_perfil, R.id.nav_linterna
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        initPermisos()
        comprobarConexion()
        initNotificaciones()
        initUI()
    }

    private fun initUI() {
        mostrarDatosUsuarioMenu()
    }

    private fun mostrarDatosUsuarioMenu() {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        val navUsername: TextView = headerView.findViewById(R.id.navHeaderUserName)
        val navUserEmail: TextView = headerView.findViewById(R.id.navHeaderUserEmail)
        val navUserImage: ImageView = headerView.findViewById(R.id.navHeaderUserImage)
        navUsername.text = Auth.currentUser?.displayName
        navUserEmail.text = Auth.currentUser?.email
        Picasso.get()
            // .load(R.drawable.user_avatar)
            .load(Auth.currentUser?.photoUrl)
            .transform(CirculoTransformacion())
            .resize(130, 130)
            .into(navUserImage)
        navUserImage.setOnClickListener { salirSesion() }
    }

    private fun salirSesion() {
        Log.i(TAG, "Saliendo...")
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_exit)
            .setTitle("Cerrar sesión actual")
            .setMessage("¿Desea salir de la sesión actual?")
            .setPositiveButton(getString(R.string.aceptar)) { dialog, which -> cerrarSesion() }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun cerrarSesion() {
        Auth.signOut()
        Log.i(TAG, "sesionDelete ok")
        Toast.makeText(applicationContext, "Sesión cerrada", Toast.LENGTH_SHORT)
            .show()
        // Y vamos a login
        val login = Intent(applicationContext, LoginActivity::class.java)
        startActivity(login)
        finish()
    }

    private fun initNotificaciones() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            // Log and toast
            Log.i(TAG, "Mi Token: $token")
        })
        FirebaseMessaging.getInstance().subscribeToTopic("estadios")

        val url = intent.getStringExtra("url")
        url.let {
            Toast.makeText(this, "Ha llegado una notificación push: $it",
                Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun comprobarConexion() {
        comprobarRed()
        comprobarGPS()
    }

    private fun comprobarRed() {
        if (Utils.isNetworkAvailable(applicationContext)) {
            Toast.makeText(applicationContext, "Existe conexión a internet", Toast.LENGTH_SHORT)
                .show()
        } else {
            val snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "Es necesaria una conexión a internet",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setActionTextColor(getColor(R.color.colorAccent))
            snackbar.setAction("Conectar") {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }
            snackbar.show()
        }
    }

    private fun comprobarGPS() {
        if (Utils.isGPSAvaliable(applicationContext)) {
            Toast.makeText(applicationContext, "Existe conexión a GPS", Toast.LENGTH_SHORT)
                .show()
        } else {
            val snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "Es necesaria una conexión a GPS",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setActionTextColor(getColor(R.color.colorAccent))
            snackbar.setAction("Conectar") {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            snackbar.show()
        }
    }

    private fun initPermisos() {
        if (!(this.application as MyApp).APP_PERMISOS)
            (this.application as MyApp).initPermisos()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}