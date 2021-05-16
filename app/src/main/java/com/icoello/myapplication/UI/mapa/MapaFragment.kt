package com.icoello.myapplication.UI.mapa

import android.app.AlertDialog
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.icoello.myapplication.Entidades.Estadio
import com.icoello.myapplication.R
import com.squareup.picasso.Picasso
import kotlin.math.ceil

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
        // Buscamos la fotografia
        val docRef = FireStore.collection("estadios").document(estadio.id)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val posicion = LatLng(estadio.latitud.toDouble(), estadio.longitud.toDouble())
                    val imageView = ImageView(context)
                    Picasso.get()
                        .load(estadio.foto)
                        .into(imageView, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                val temp = (imageView.drawable as BitmapDrawable).bitmap
                                val pin: Bitmap = crearPin(temp)!!
                                val marker = mMap.addMarker(
                                    MarkerOptions() // Posición
                                        .position(posicion) // Título
                                        .title(estadio.nombre) // Subtitulo
                                        .snippet(estadio.equipo + ", capacidad " + estadio.capacidad) // Color o tipo d icono
                                        .anchor(0.5f, 0.907f)
                                        .icon(BitmapDescriptorFactory.fromBitmap(pin))
                                )
                                // Le añado como tag el lugar para recuperarlo
                                marker.tag = estadio
                            }

                            override fun onError(e: Exception) {
                                Log.d(TAG, "Error al descargar imagen")
                            }
                        })

                } else {
                    Log.i(TAG, "Error: No exite fotografía")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "ERROR: " + exception.localizedMessage)
            }
    }

    private fun crearPin(bitmap: Bitmap?): Bitmap? {
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(dp(62f), dp(76f), Bitmap.Config.ARGB_8888)
            result.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(result)
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location)
            drawable?.setBounds(0, 0, dp(62f), dp(76f))
            drawable?.draw(canvas)
            val roundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val bitmapRect = RectF()
            canvas.save()
            //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
            if (bitmap != null) {
                val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                val matrix = Matrix()
                val scale = dp(52f) / bitmap.width.toFloat()
                matrix.postTranslate(dp(5f).toFloat(), dp(5f).toFloat())
                matrix.postScale(scale, scale)
                roundPaint.shader = shader
                shader.setLocalMatrix(matrix)
                bitmapRect[dp(5f).toFloat(), dp(5f).toFloat(), dp(52f + 5).toFloat()] = dp(52f + 5).toFloat()
                canvas.drawRoundRect(bitmapRect, dp(26f).toFloat(), dp(26f).toFloat(), roundPaint)
            }
            canvas.restore()
            try {
                canvas.setBitmap(null)
            } catch (e: Exception) {
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return result
    }

    private fun dp(value: Float): Int {
        return if (value == 0f) {
            0
        } else
            ceil((resources.displayMetrics.density * value).toDouble()).toInt()
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