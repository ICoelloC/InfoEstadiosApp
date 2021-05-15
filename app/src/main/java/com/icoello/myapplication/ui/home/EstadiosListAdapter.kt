package com.icoello.myapplication.ui.home

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.icoello.myapplication.Entidades.Estadio
import com.icoello.myapplication.R
import com.squareup.picasso.Picasso

class EstadiosListAdapter(
    private val listaEstadios: MutableList<Estadio>,
    private val accionPrincipal: (Estadio) -> Unit,
) : RecyclerView.Adapter<EstadiosListAdapter.EstadioViewHolder>() {

    private var FireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "ADAPTER"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstadioViewHolder {
        return EstadioViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_estadio, parent, false)
        )
    }

    override fun onBindViewHolder(holder: EstadioViewHolder, viewType: Int) {

    }

    fun removeItem(position: Int) {
        listaEstadios.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaEstadios.size)
    }

    fun updateItem(item: Estadio, position: Int) {
        listaEstadios[position] = item
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaEstadios.size)
    }

    fun addItem(item: Estadio) {
        listaEstadios.add(item)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listaEstadios.size
    }

    /*
    private fun imagenEstadio(estadio: Estadio, holder: EstadioViewHolder) {
        val docRef = FireStore.collection("estadios").document(estadio.foto)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val miImagen = document.toObject(Estadio::class.java)
                    Log.i(TAG, "fotografiasGetById ok: ${document.data}")
                    Picasso.get()
                        // .load(R.drawable.user_avatar)
                        .load(miImagen?.uri)
                        .into(holder.itemLugarImagen)
                } else {
                    Log.i(TAG, "Error: No exite fotografía")
                    imagenPorDefecto(holder)
                }
            }
    }
     */
/*
    private fun imagenPorDefecto(holder: EstadioViewHolder) {
        holder.itemEstadioFoto.setImageBitmap(
            BitmapFactory.decodeResource(
                holder.context?.resources,
                R.drawable.logo
            )
        )

    }
*/
/*
    private fun eventoBotonSeguir(position: Int, holder: EstadioViewHolder){
        listaEstadios[position].seguidores != listaEstadios[position].seguidores
        colorBotonFavorito(position, holder)
        if (listaEstadios[position].se)
    }
*/
    class EstadioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //var itemEstadioFoto = itemView.itemEstadioFoto
        //var itemLugarNomnre = itemView.item
        //var context = itemView.context
    }

}

