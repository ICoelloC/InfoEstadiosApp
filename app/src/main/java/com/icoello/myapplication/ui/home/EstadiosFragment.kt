package com.icoello.myapplication.ui.home

import android.graphics.*
import android.os.Bundle
import android.text.TextUtils.indexOf
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.Iterables.indexOf
import com.google.common.collect.Iterators.indexOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.icoello.myapplication.Entidades.Estadio
import com.icoello.myapplication.R

class EstadiosFragment : Fragment() {

    private lateinit var Auth: FirebaseAuth
    private lateinit var FireStore: FirebaseFirestore

    private var ESTADIOS = mutableListOf<Estadio>()

    private lateinit var estadiosAdapter: EstadiosListAdapter
    private var paintSweep = Paint()
    private lateinit var USUARIO: FirebaseUser

    companion object {
        private const val TAG = "Estadios"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        return inflater.inflate(R.layout.fragment_estadios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Auth = Firebase.auth
        FireStore = FirebaseFirestore.getInstance()
        this.USUARIO = Auth.currentUser!!
        initUI()
    }

    private fun initUI() {
        Log.i(TAG, "Init IU")
        iniciarSwipeRecarga()
        cargarLugares()
        iniciarSpinner()
        iniciarSwipeHorizontal()
        lugaresRecycler.layoutManager = LinearLayoutManager(context)
        lugaresFabNuevo.setOnClickListener { nuevoElemento() }
        lugaresButtonVoz.setOnClickListener { controlPorVoz() }

        Log.i(TAG, "Fin la IU")
    }

    private fun iniciarSwipeRecarga() {
        estadiosSwipeRefresh.setColorSchemeResources(R.color.primaryLightColor)
        estadiosSwipeRefresh.setProgressBackgroundColorSchemeResource(R.color.white)
        estadiosSwipeRefresh.setOnRefreshListener {
            cargarEstadios()
        }
    }

    private fun iniciarSwipeHorizontal() {
        val simpleItemToucgCallback: ItemTouchHelper.SimpleCallback =
            object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val posiion = viewHolder.adapterPosition
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            borrarElemento(position)
                        }
                        else -> {
                            editarElemento(position)
                        }
                    }
                }

                override fun onChildDraw(
                    canvas: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        val itemView = viewHolder.itemView
                        val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                        val width = height / 3
                        if (dX > 0) {
                            botonIzquierdo(canvas, dX, itemView, width)
                        } else {
                            botonDerecho(canvas, dX, itemView, width)
                        }
                    }
                    super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }
        val itemTouchHelper = ItemTouchHelper(simpleItemToucgCallback)
        itemTouchHelper.attachToRecyclerView(estadiosRecycler)
    }

    private fun botonDerecho(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        paintSweep.color = Color.RED
        val background = RectF(
            itemView.right.toFloat() + dX,
            itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_delete)
        val iconDest = RectF(
            itemView.right.toFloat() - 2 * width,
            itemView.top.toFloat() + width,
            itemView.right.toFloat() - width,
            itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    private fun botonIzquierdo(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        paintSweep.color = Color.GREEN
        val background = RectF(
            itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable)
    }

    private fun cargarEstadios() {
        ESTADIOS.clear()
        estadiosSwipeRefresh.isRefreshing = true
        estadiosAdapter = EstadiosListAdapter(ESTADIOS) {
            eventoClicFila(it)
        }
        estadiosRecycler.adapter = estadiosAdapter
        Toast.makeText(context, "Obteniendo estadios", Toast.LENGTH_LONG).show()
        FireStore.collection("estadios")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Toast.makeText(
                        context,
                        "Error al acceder al servicio: " + e.localizedMessage,
                        Toast.LENGTH_LONG
                    )
                        .show()
                    return@addSnapshotListener
                }
                estadiosSwipeRefresh.isRefreshing = false
                for (doc in value!!.documentChanges) {
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.i(TAG, "ADDED  ${doc.document.data}")
                            insertarDocumento(doc.document.data)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.i(TAG, "MODIFIED: ${doc.document.data}")
                            modificarDocumento(doc.document.data)
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.i(TAG, "REMOVED: ${doc.document.data}")
                            eliminarDocumento(doc.document.data)
                        }
                    }
                }
            }
    }

    private fun eliminarDocumento(doc: Map<String, Any>) {
        val miEstadio = documentToLugar(doc)
        Log.i(TAG, "Elimando lugar: ${miEstadio.id}")
        // Buscamos que esté
        val index = Estadio.indexOf(miEstadio)
        if (index >= 0)
            eliminarItemLista(index)
    }

    private fun modificarDocumento(doc: Map<String, Any>) {
        val miEstadio = documentToLugar(doc)
        Log.i(TAG, "Modificando lugar: ${miEstadio.id}")
        // Buscamos que esté,
        val index = ESTADIOS.indexOf(miEstadio)
        if (index >= 0)
            actualizarItemLista(miEstadio, index)
    }

    private fun eliminarItemLista(position: Int) {
        this.estadiosAdapter.removeItem(position)
        estadiosAdapter.notifyDataSetChanged()
    }

    private fun eventoClicFila(estadio: Estadio) {
        abrirElemento(estadio)
    }

    private fun abrirElemento(estadio: Estadio) {
        Log.i(TAG, "Visualizando el elemento: ${estadio.id}")
        abrirDetalle(estadio)
    }

    private fun abrirDetalle(estadio: Estadio?) {
        Log.i("Estadios", "Abriendo el elemento pos: " + estadio?.id)
        val estadioDetalle = EstadioDetalleFragment(estadio)
        actualizarVistaEstadio()
    }

    private fun actualizarVistaEstadio() {
        estadiosRecycler.adapter = estadiosAdapter
    }

    private fun documentToLugar(doc: Map<String, Any>) = Estadio(
        id = doc["id"].toString(),
        nombre = doc["nombre"].toString(),
        capacidad = doc["capacidad"].toString().toInt(),
        equipo = doc["equipo"].toString(),
        latitud = doc["latitud"].toString(),
        longitud = doc["longitud"].toString(),
        seguidores = doc["seguidores"].toString().toInt(),
        id_usuario = (doc["id_usuario"].toString()),
        foto = doc["foto"].toString(),
        seguido = doc["seguido"].toString().toBoolean()
    )

    private fun insertarDocumento(doc: MutableMap<String, Any>) {
        val miEstadio = documentToEstadio(doc)
        Log.i(TAG, "Añadiendo lugar: ${miLugar.id}")
        // Buscamos que no este... para evitar distintas llamadas de ADD
        val existe = LUGARES.any { lugar -> lugar.id == miLugar.id }
        if (!existe)
            insertarItemLista(miLugar)
    }

}

