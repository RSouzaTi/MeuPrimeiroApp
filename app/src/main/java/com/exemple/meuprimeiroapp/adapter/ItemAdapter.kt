package com.exemple.meuprimeiroapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.exemple.meuprimeiroapp.R
import com.exemple.meuprimeiroapp.model.Item
import com.exemple.meuprimeiroapp.ui.loadUrl

class ItemAdapter(
    private val items: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image)
        val fullNameTextView: TextView = view.findViewById(R.id.name)
        val ageTextView: TextView = view.findViewById(R.id.age)
        val professionTextView: TextView = view.findViewById(R.id.profession)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]

        // IMPORTANTE: Como vimos no seu Logcat, os dados estão dentro de 'value'
        // Se acessarmos item.name diretamente, virá NULL.
        val itemData = item.value

        holder.fullNameTextView.text = itemData.fullName
        holder.ageTextView.text = holder.itemView.context.getString(R.string.item_age, itemData.age)
        holder.professionTextView.text = itemData.profession

        // Carrega a imagem usando a URL que está dentro de value
        holder.imageView.loadUrl(itemData.imageUrl)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}