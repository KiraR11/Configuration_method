package com.example.metod_konfigyrati

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.metod_konfigyrati.databinding.IterItemBinding

class IterAdapter(private var coords: ArrayList<Coordinates>)
    : RecyclerView.Adapter<IterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IterViewHolder {
        //val itemView :View = LayoutInflater.from(parent.context).inflate(R.layout.item_card,parent,false)
        val itemBinding = IterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IterViewHolder(itemBinding)
    }
    override fun getItemCount() = coords.size
    override fun onBindViewHolder(holder: IterViewHolder, position: Int) {
        holder.bind(coords[position])
    }
}