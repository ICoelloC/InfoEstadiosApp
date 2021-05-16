package com.icoello.myapplication.ui.estadios

import android.app.Activity.RESULT_CANCELED
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toFile
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
import com.icoello.myapplication.Utilidades.Fotos
import kotlinx.android.synthetic.main.fragment_estadio_detalle.*
import java.io.IOException
import java.util.*

class EstadioDetalle(
    private var ESTADIO: Estadio? = null,
    private val MODO: Modo? = Modo.INSERTAR
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
        when (this.MODO) {
            Modo.INSERTAR -> initModoInsertar()
            Modo.VISUALIZA -> initModoVisualizar()
            Modo.ELIMINAR -> initModoEliminar()
            Modo.ACTUALIZAR -> initModoActualizar()
            else -> {
            }
        }
        leerPoscionGPSActual()
        initMapa()
    }

    private fun initUsuario() {
        this.USUARIO = Auth.currentUser!!
    }

    private fun initPermisos() {
        this.PERMISOS = (activity?.application as MyApp).APP_PERMISOS
    }

    private fun initModoInsertar() {
        Log.i("Estadios", "Modo Insertar")
        detalleEstadioSeguidores.visibility = View.GONE
        detalleEstadioInputNombre.setText("")
        detalleEstadioSeguidores.text = "0 seguidores"
        detalleEstadioEditarBtn.visibility = View.GONE
        detalleEstadioBorrarBtn.visibility = View.GONE
        detalleEstadioGuardarBtn.setOnClickListener { insertarEstadio() }
        detalleEstadioFabCamara.setOnClickListener { initDialogFoto() }

    }

    private fun initModoVisualizar() {
        Log.i("Estadios", "Modo Visualizar")
        detalleEstadioInputNombre.setText(ESTADIO?.nombre)
        detalleEstadioInputNombre.isEnabled = false
        detalleEstadioSeguidores.text = ESTADIO?.seguidores.toString() + " seguidores"
        (ESTADIO?.capacidad)?.let { detalleEstadioInputCapacidad.setText(it.toInt()) }
        detalleEstadioInputCapacidad.isEnabled = false
        detalleEstadioInputEquipo.setText(ESTADIO?.equipo)
        detalleEstadioEditarBtn.visibility = View.GONE
        detalleEstadioBorrarBtn.visibility = View.GONE
        detalleEstadioGuardarBtn.visibility = View.GONE
        detalleEstadioFabCamara.visibility = View.GONE
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

    private fun initModoEliminar() {
        Log.i("Estadios", "Modo Eliminar")
        initModoVisualizar()
        detalleEstadioBorrarBtn.visibility = View.GONE
        detalleEstadioBorrarBtn.setOnClickListener { eliminarEstadio() }
    }

    private fun initModoActualizar() {
        Log.i("Estadios", "Modo Actualizar")
        initModoVisualizar()
        detalleEstadioEditarBtn.visibility = View.VISIBLE
        detalleEstadioInputNombre.isEnabled = true
        detalleEstadioInputEquipo.isEnabled = true
        detalleEstadioInputCapacidad.isEnabled = true
        detalleEstadioFabCamara.visibility = View.VISIBLE
        detalleEstadioFabCamara.setOnClickListener { initDialogFoto() }
        detalleEstadioEditarBtn.setOnClickListener { actualizarEstadio() }
    }

    private fun insertarEstadio() {
        if (comprobarFormulario()) {
            alertaDialogo("Insertar Estadio", "¿Desea salvar este estadio?")
        }
    }

    private fun insertar() {
        val id_foto = UUID.randomUUID().toString()

        ESTADIO = Estadio(
            id = UUID.randomUUID().toString(),
            nombre = detalleEstadioInputNombre.text.toString().trim(),
            capacidad = detalleEstadioInputCapacidad.text.toString().trim().toInt(),
            equipo = detalleEstadioInputEquipo.text.toString().trim(),
            latitud = posicion?.latitude.toString(),
            longitud = posicion?.longitude.toString(),
            seguidores = 0,
            id_usuario = USUARIO.uid,
            foto = "",
            seguido = false
        )
        FireStore.collection("estadios")
            .document(ESTADIO!!.id)
            .set(ESTADIO!!)
            .addOnSuccessListener {
                insertarFoto()
                Log.i(TAG, "Estadio insertado con éxito con id: $ESTADIO")
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error insertar lugar", e) }
    }

    private fun insertarFoto() {

    }

    private fun eliminarEstadio() {
        alertaDialogo("Eliminar Estadio", "¿Quieres eliminarlo?")
    }

    private fun eliminar() {
        FireStore.collection("estadios")
            .document(ESTADIO!!.id)
            .delete()
            .addOnSuccessListener {
                Log.i(TAG, "Estadio eliminado con éxito")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
            }
    }

    private fun actualizarEstadio() {
        if (comprobarFormulario()) {
            alertaDialogo("Modificar Estadio", "¿Desea modificar este estadio?")
        }
    }

    private fun actualizar() {
        with(ESTADIO!!) {
            nombre = detalleEstadioInputNombre.text.toString().trim()
            equipo = detalleEstadioInputEquipo.text.toString().trim()
            latitud = posicion?.latitude.toString()
            longitud = posicion?.longitude.toString()
            capacidad = (detalleEstadioInputCapacidad.toString().trim()).toInt()
        }
        FireStore.collection("estadios")
            .document(ESTADIO!!.id)
            .set(ESTADIO!!)
            .addOnSuccessListener {
                Log.i(TAG, "Estadio actualizado con éxito con id: " + ESTADIO!!.id)
                /*
                if (IMAGEN_URI.toString() != detalleEstadioImagen.toString()) {
                    actualizarFotografia()
                } else {
                    Snackbar.make(view!!, "¡Lugar actualizado con éxito!", Snackbar.LENGTH_LONG).show()
                    volver()
                }
                 */
            }
    }

    private fun volver(){
        activity?.onBackPressed()
    }

    private fun comprobarFormulario(): Boolean {
        var sal = true
        if (detalleEstadioInputNombre.text?.isEmpty()!!) {
            detalleEstadioInputNombre.error = "El nombre del estadio no puede estar vacío"
            sal = false
        }
        if (detalleEstadioInputCapacidad.text?.isEmpty()!!) {
            detalleEstadioInputCapacidad.error = "La capacidad del estadio no puede estar vacía"
            sal = false
        }
        if (detalleEstadioInputEquipo.text?.isEmpty()!!) {
            detalleEstadioInputEquipo.error = "El equipo no puede estar vacío"
            sal = false
        }
        if (!this::FOTO.isInitialized) {
            this.FOTO = (detalleEstadioImagen.drawable as BitmapDrawable).bitmap
            Toast.makeText(context, "La imagen no puede estar vacía", Toast.LENGTH_SHORT).show()
            sal = false
        }
        return sal
    }

    private fun alertaDialogo(titulo: String, texto: String) {
        val builder = AlertDialog.Builder(context)
        with(builder)
        {
            setIcon(R.drawable.logo)
            setTitle(titulo)
            setMessage(texto)
            setPositiveButton(R.string.aceptar) { _, _ ->
                when (MODO) {
                    Modo.INSERTAR -> insertar()
                    Modo.ELIMINAR -> eliminar()
                    Modo.ACTUALIZAR -> actualizar()
                    else -> {
                    }
                }
            }
            setNegativeButton(R.string.cancelar, null)
            // setNeutralButton("Maybe", neutralButtonClick)
            show()
        }
    }

    private fun leerPoscionGPSActual() {
        mPosicion = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun initMapa() {
        Log.i("Mapa", "Iniciando Mapa")
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.detalleEstadioMapa) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarIUMapa()
        modoMapa()
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

    private fun modoMapa() {
        Log.i("Mapa", "Configurando Modo Mapa")
        when (this.MODO) {
            Modo.INSERTAR -> mapaInsertar()
            Modo.VISUALIZA -> mapaVisualizar()
            Modo.ELIMINAR -> mapaVisualizar()
            Modo.ACTUALIZAR -> mapaActualizar()
            else -> {
            }
        }
    }

    private fun mapaInsertar() {
        Log.i("Mapa", "Configurando Modo Insertar")
        if (this.PERMISOS) {
            mMap.isMyLocationEnabled = true
        }
        activarEventosMarcadores()
        obtenerPosicion()
    }

    private fun mapaVisualizar() {
        Log.i("Mapa", "Configurando Modo Visualizar")
        posicion = LatLng(ESTADIO!!.latitud.toDouble(), ESTADIO!!.longitud.toDouble())
        mMap.addMarker(
            MarkerOptions() // Posición
                .position(posicion!!) // Título
                .title(ESTADIO!!.nombre) // Subtitulo
                .snippet(ESTADIO!!.capacidad.toString() + " del " + ESTADIO!!.equipo) // Color o tipo d icono
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion))
    }

    private fun mapaActualizar() {
        Log.i("Mapa", "Configurando Modo Actualizar")
        if (this.PERMISOS) {
            mMap.isMyLocationEnabled = true
        }
        activarEventosMarcadores()
        mapaVisualizar()
    }

    private fun activarEventosMarcadores() {
        mMap.setOnMapClickListener { point -> // Creamos el marcador
            // Borramos el marcador Touch si está puesto
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

    private fun obtenerPosicion() {
        Log.i("Mapa", "Opteniendo posición")
        try {
            if (this.PERMISOS) {
                val local: Task<Location> = mPosicion!!.lastLocation
                local.addOnCompleteListener(
                    requireActivity()
                ) { task ->
                    if (task.isSuccessful) {
                        localizacion = task.result
                        posicion = LatLng(
                            localizacion!!.latitude,
                            localizacion!!.longitude
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion))
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

    override fun onMarkerClick(marker: Marker?): Boolean {
        Log.i("Mapa", marker.toString())
        return false
    }

    private fun initDialogFoto() {
        val fotoDialogoItems = arrayOf(
            "Seleccionar fotografía de galería",
            "Capturar fotografía desde la cámara"
        )
        // Creamos el dialog con su builder
        AlertDialog.Builder(context)
            .setTitle("Seleccionar Acción")
            .setItems(fotoDialogoItems) { dialog, modo ->
                when (modo) {
                    0 -> elegirFotoGaleria()
                    1 -> tomarFotoCamara()
                }
            }
            .show()
    }

    private fun elegirFotoGaleria() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALERIA)
    }

    private fun tomarFotoCamara() {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val nombre = Fotos.crearNombreFoto(IMAGEN_PREFIJO, IMAGEN_EXTENSION)
        val fichero = Fotos.salvarFoto(IMAGEN_DIRECTORY, nombre, requireContext())
        IMAGEN_URI = Uri.fromFile(fichero)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGEN_URI)
        Log.i("Camara", IMAGEN_URI.path.toString())
        startActivityForResult(intent, CAMARA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("FOTO", "Opción:--->$requestCode")
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_CANCELED){
            Log.i("FOTO", "Se ha cancelado")
        }
        if (requestCode == GALERIA) {
            Log.i("FOTO", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI con su dirección temporal
                val contentURI = data.data!!
                try {
                    // Obtenemos el bitmap de su almacenamiento externo
                    // Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    if (Build.VERSION.SDK_INT < 28) {
                        this.FOTO = MediaStore.Images.Media.getBitmap(context?.contentResolver, contentURI)
                    } else {
                        val source: ImageDecoder.Source =
                            ImageDecoder.createSource(context?.contentResolver!!, contentURI)
                        this.FOTO = ImageDecoder.decodeBitmap(source)
                    }
                    // Para jugar con las proporciones y ahorrar en memoria no cargando toda la foto, solo carga 600px max
                    val prop = this.IMAGEN_PROPORCION / this.FOTO.width.toFloat()
                    // Actualizamos el bitmap para ese tamaño, luego podríamos reducir su calidad
                    this.FOTO = Bitmap.createScaledBitmap(
                        this.FOTO,
                        this.IMAGEN_PROPORCION,
                        (this.FOTO.height * prop).toInt(),
                        false
                    )
                    // Vamos a copiar nuestra imagen en nuestro directorio comprimida por si acaso.
                    val nombre = Fotos.crearNombreFoto(IMAGEN_PREFIJO, IMAGEN_EXTENSION)
                    val fichero =
                        Fotos.copiarFoto(this.FOTO, nombre, IMAGEN_DIRECTORY, IMAGEN_COMPRESION, requireContext())
                    IMAGEN_URI = Uri.fromFile(fichero)
                    Toast.makeText(context, "¡Foto rescatada de la galería!", Toast.LENGTH_SHORT).show()
                    detalleEstadioImagen.setImageBitmap(this.FOTO)

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMARA) {
            Log.i("FOTO", "Entramos en Camara")
            // Cogemos la imagen, pero podemos coger la imagen o su modo en baja calidad (thumbnail)
            try {
                if (Build.VERSION.SDK_INT < 28) {
                    this.FOTO = MediaStore.Images.Media.getBitmap(context?.contentResolver, IMAGEN_URI)
                } else {
                    val source: ImageDecoder.Source = ImageDecoder.createSource(context?.contentResolver!!, IMAGEN_URI)
                    this.FOTO = ImageDecoder.decodeBitmap(source)
                }
                // Comprimimos la foto
                Log.i("Camara", IMAGEN_URI.path.toString())
                Fotos.comprimirFoto(IMAGEN_URI.toFile(), this.FOTO, this.IMAGEN_COMPRESION)
                // Mostramos
                detalleEstadioImagen.setImageBitmap(this.FOTO)
                Toast.makeText(context, "¡Foto Salvada!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "¡Fallo Camara!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}