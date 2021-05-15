package com.icoello.myapplication.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.icoello.myapplication.App.MyApp
import com.icoello.myapplication.Entidades.Estadio
import com.icoello.myapplication.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_estadio_detalle.*

class EstadioDetalle(
    private var ESTADIO: Estadio? = null
) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // Firebase
    private lateinit var Auth: FirebaseAuth
    private lateinit var FireStore: FirebaseFirestore
    private lateinit var Storage: FirebaseStorage

    private lateinit var USUARIO: FirebaseUser
    private var PERMISOS: Boolean = false

    private lateinit var mMap: GoogleMap
    private var mPosicion: FusedLocationProviderClient? = null
    private var marcadorTouch: Marker? = null
    private var localizacion: Location? = null
    private var posicion: LatLng? = null

    private val GALERIA = 1
    private val CAMARA = 2
    private lateinit var IMAGEN_URI: Uri
    private val IMAGEN_DIRECTORY = "/InfoEstadios"
    private val IMAGEN_PROPORCION = 600
    private lateinit var FOTO: Bitmap
    private var IMAGEN_COMPRESION = 60
    private val IMAGEN_PREFIJO = "lugar"
    private val IMAGEN_EXTENSION = ".jpg"

    companion object {
        private const val TAG = "Estadio"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_estadio_detalle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Auth = Firebase.auth
        FireStore = FirebaseFirestore.getInstance()
        Storage = FirebaseStorage.getInstance()
        Log.i(TAG, "Creando Estadio Detalle")
        view.setOnTouchListener { view, motionEvent ->
            return@setOnTouchListener true
        }
        initIU()

    }

    private fun initIU() {
        initPermisos()
        initUsuario()
        iniciarFormulario()
        leerPoscionGPSActual()
        initMapa()
    }

    private fun initPermisos() {
        this.PERMISOS = (activity?.application as MyApp).APP_PERMISOS
    }

    private fun initUsuario() {
        this.USUARIO = Auth.currentUser!!
    }

    private fun iniciarFormulario() {

    }

    private fun cagarFotografia() {

    }

    private fun imagenPorDefecto() {
        detalleEstadioImagen.setImageBitmap(
            BitmapFactory.decodeResource(
                context?.resources,
                R.drawable.logo
            )
        )
    }

    private fun initMapa() {
        Log.i("Mapa", "Iniciando Mapa")
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.detalleEstadioMapa) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    private fun leerPoscionGPSActual() {
        mPosicion = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarIUMapa()
        modoMapa()
    }

    private fun modoMapa() {
        /*
        if(this.PERMISOS){
            mMap.isMyLocationEnabled = true
        }
        activarEventosMarcadores()
        obtenerPosicion()
         */
    }

    private fun obtenerPosicion() {
        Log.i("Mapa", "Opteniendo posición")
        try {
            if (this.PERMISOS) {
                // Lo lanzamos como tarea concurrente
                val local: Task<Location> = mPosicion!!.lastLocation
                local.addOnCompleteListener(
                    requireActivity()
                ) { task ->
                    if (task.isSuccessful) {
                        // Actualizamos la última posición conocida
                        //try {
                        localizacion = task.result
                        posicion = LatLng(
                            localizacion!!.latitude,
                            localizacion!!.longitude
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion))
                        //}catch (ex: Exception) {
                        //   Snackbar.make(view!!, "GPS Inactivo o sin posición actual", Snackbar.LENGTH_LONG).show();
                        //}
                    } else {
                        Log.i("GPS", "No se encuetra la última posición.")
                        Log.e("GPS", "Exception: %s", task.exception)
                    }
                }
            }
        } catch (e: SecurityException) {
            Snackbar.make(
                requireView(),
                "No se ha encontrado su posoción actual o el GPS está desactivado",
                Snackbar.LENGTH_LONG
            ).show()
            Log.e("Exception: %s", e.message.toString())
        }
    }

    private fun activarEventosMarcadores() {
        mMap.setOnMapClickListener { point ->
            marcadorTouch?.remove()
            marcadorTouch = mMap.addMarker(
                MarkerOptions() // Posición
                    .position(point) // Título
                    .title("Posición Actual") // Subtitulo
                    .snippet(detalleEstadioInputNombre.text.toString()) // Color o tipo d icono
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
            posicion = point
        }
    }

    private fun configurarIUMapa() {
        Log.i("Mapa", "Configurando IU Mapa")
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val uiSettings: UiSettings = mMap.uiSettings
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        uiSettings.isCompassEnabled = true
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isMapToolbarEnabled = true
        mMap.setMinZoomPreference(12.0f)
        mMap.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("Not yet implemented")
    }


}