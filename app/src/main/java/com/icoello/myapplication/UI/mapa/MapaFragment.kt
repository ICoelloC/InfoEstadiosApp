package com.icoello.myapplication.UI.mapa

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.icoello.myapplication.Entidades.Estadio
import com.icoello.myapplication.R
import com.squareup.picasso.Picasso

class MapaFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var Auth: FirebaseAuth
    private lateinit var FireStore: FirebaseFirestore

    private lateinit var mMap: GoogleMap
    private lateinit var USUARIO: FirebaseUser

    companion object {
        private const val TAG = "MAPA"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mapa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Auth = Firebase.auth
        FireStore = FirebaseFirestore.getInstance()
        view.setOnTouchListener { view, motionEvent ->
            return@setOnTouchListener true
        }

        this.USUARIO = Auth.currentUser!!
        initUI()
    }

    private fun initUI() {
        initMapa()
    }

    private fun initMapa() {
        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.miMapa) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarUIMapa()
        puntosMapa()
    }

    private fun puntosMapa() {
        FireStore.collection("estadios")
            .whereEqualTo("id_usuario", USUARIO.uid)
            .get()
            .addOnSuccessListener { result ->
                val listaEstadios = mutableListOf<Estadio>()
                for (document in result) {
                    val miLugar = document.toObject(Estadio::class.java)
                    listaEstadios.add(miLugar)
                }
                procesarEstadios(listaEstadios)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context,
                    "Error al acceder al servicio: " + exception.localizedMessage,
                    Toast.LENGTH_LONG)
                    .show()
            }
    }

    private fun procesarEstadios(listaEstadios: MutableList<Estadio>) {
        listaEstadios.forEach{
            addMarcador(it)
        }
        actualizarCamara(listaEstadios)
        mMap.setOnMarkerClickListener(this)
    }

    private fun addMarcador(estadio: Estadio) {

    }

    private fun actualizarCamara(listaEstadios: MutableList<Estadio>?) {
        val bc = LatLngBounds.Builder()
        for (item in listaEstadios!!){
            bc.include(LatLng(item.latitud.toDouble(), item.longitud.toDouble()))
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 120))
    }

    private fun configurarUIMapa() {
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val uiConfig: UiSettings = mMap.uiSettings
        uiConfig.isScrollGesturesEnabled = true
        uiConfig.isTiltGesturesEnabled = true
        uiConfig.isCompassEnabled = true
        uiConfig.isZoomControlsEnabled = true
        uiConfig.isMapToolbarEnabled = true
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val estadio = marker.tag as Estadio
        mostrarDialogo(estadio)
        return false
    }

    private fun mostrarDialogo(estadio: Estadio) {
        val builder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val vista = inflater.inflate(R.layout.intem_visualizacion_mapa, null)
        // Le ponemos las cosas
        val nombre = vista.findViewById(R.id.mapaEstadioTextNombre) as TextView
        nombre.text = estadio.nombre
        val imagen = vista.findViewById(R.id.mapaEstadioImagen) as ImageView
        val docRef = FireStore.collection("estadios").document(estadio.foto)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val miImagen = estadio.foto
                    Picasso
                        .get()
                        .load(miImagen)
                        .into(imagen)
                } else {
                    Log.i(TAG, "Error: No exite fotografía")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "ERROR: " + exception.localizedMessage)
            }

        builder
            .setView(vista)
            .setIcon(R.drawable.ic_location)
            .setTitle("Estadio")
            .setPositiveButton(R.string.aceptar) { _, _ ->
                null
            }
        builder.show()
    }
}