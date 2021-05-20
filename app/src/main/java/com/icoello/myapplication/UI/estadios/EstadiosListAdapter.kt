package com.icoello.myapplication.UI.estadios

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.icoello.myapplication.Entidades.Estadio
import com.icoello.myapplication.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_estadio.view.*

class EstadiosListAdapter(
    private val listaEstadios: MutableList<Estadio>,
    private val accionPrincipal: (Estadio) -> Unit,
) : RecyclerView.Adapter<EstadiosListAdapter.EstadiosViewHolder>() {

    private var fireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "Adapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstadiosViewHolder {
        return EstadiosViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_estadio, parent, false)
        )
    }

    override fun onBindViewHolder(holder: EstadiosViewHolder, position: Int) {
        holder.itemNombre.text = listaEstadios[position].nombre
        holder.itemSeguidores.text = listaEstadios[position].seguidores.toString()
        cargFotoEstadio(listaEstadios[position], holder)
        colorSeguir(position, holder)
        holder.itemSeguir.setOnClickListener {
            eventoBotonSeguir(position, holder)
        }

        holder.itemFoto.setOnClickListener {
            accionPrincipal(listaEstadios[position])
        }
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

    private fun cargFotoEstadio(estadio: Estadio, holder: EstadiosViewHolder) {
        if (estadio.foto != "") {
            Picasso.get()
                .load(estadio.foto)
                .into(holder.itemFoto)
        } else {
            imagenPorDefecto(holder)
        }
    }

    private fun imagenPorDefecto(holder: EstadiosViewHolder) {
        holder.itemFoto.setImageBitmap(
            BitmapFactory.decodeResource(
                holder.context?.resources,
                R.drawable.logo
            )
        )
    }

    private fun eventoBotonSeguir(position: Int, holder: EstadiosViewHolder) {
        listaEstadios[position].seguido = !listaEstadios[position].seguido
        colorSeguir(position, holder)
        if (listaEstadios[position].seguido)
            listaEstadios[position].seguidores++
        else
            listaEstadios[position].seguidores--

        actualizarEstadioSeguidores(listaEstadios[position])
    }

    private fun actualizarEstadioSeguidores(estadio: Estadio) {
        val estadioRef = fireStore.collection("estadios").document(estadio.id)
        estadioRef
            .update(
                mapOf(
                    "seguidores" to estadio.seguidores,
                    "seguido" to estadio.seguido
                )
            )
            .addOnSuccessListener {
                Log.i(TAG, "lugarUpdate ok")
            }.addOnFailureListener { e -> Log.w(TAG, "Error actualiza votos", e) }
    }

    private fun colorSeguir(position: Int, holder: EstadiosViewHolder) {
        if (listaEstadios[position].seguido)
            holder.itemSeguir.backgroundTintList =
                AppCompatResources.getColorStateList(holder.context, R.color.seguirOn)
        else
            holder.itemSeguir.backgroundTintList =
                AppCompatResources.getColorStateList(holder.context, R.color.seguirOff)
    }

    class EstadiosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemFoto: ImageView = itemView.itemEstadioFoto
        var itemNombre: TextView = itemView.itemEstadioNombre
        var itemSeguidores: TextView = itemView.itemEstadioSeguidores
        var itemSeguir: FloatingActionButton = itemView.itemEstadioSeguir
        var context = itemView.context
    }

}

