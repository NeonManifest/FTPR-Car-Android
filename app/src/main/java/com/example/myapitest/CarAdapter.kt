package com.example.myapitest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CarAdapter : ListAdapter<Carro, CarAdapter.ViewHolder>(CarDiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image)
        val modelTextView: TextView = itemView.findViewById(R.id.model)
        val yearTextView: TextView = itemView.findViewById(R.id.year)
        val licenseTextView: TextView = itemView.findViewById(R.id.license)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val car = getItem(position) // Use getItem() instead of carList[position]
        holder.modelTextView.text = car.name
        holder.yearTextView.text = car.year.toString()
        holder.licenseTextView.text = car.licence
        Picasso.get().load(car.imageUrl).into(holder.imageView)
    }

    private class CarDiffCallback : DiffUtil.ItemCallback<Carro>() {
        override fun areItemsTheSame(oldItem: Carro, newItem: Carro): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Carro, newItem: Carro): Boolean {
            return oldItem == newItem
        }
    }

}