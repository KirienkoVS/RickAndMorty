package com.example.rickandmorty.ui.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.R
import com.example.rickandmorty.domain.model.LocationEntity
import com.example.rickandmorty.ui.view.LocationsFragmentDirections

class LocationPagingAdapter: PagingDataAdapter<LocationEntity, RecyclerView.ViewHolder>(LOCATION_COMPARATOR) {

    class LocationViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.location_name)
        private val type: TextView = view.findViewById(R.id.location_type)
        private val dimension: TextView = view.findViewById(R.id.location_dimension)

        fun bind(location: LocationEntity) {
            name.text = location.name
            type.text = location.type.ifBlank { "unknown" }
            dimension.text = location.dimension.ifBlank { "unknown" }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LocationViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.location_item, parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { location ->
            (holder as LocationViewHolder).bind(location)

            holder.itemView.setOnClickListener { view ->
                val action = LocationsFragmentDirections.actionLocationsPageToLocationDetailsFragment(locationID = location.id)
                view.findNavController().navigate(action)
            }
        }
    }

    companion object {
        private val LOCATION_COMPARATOR = object : DiffUtil.ItemCallback<LocationEntity>() {
            override fun areItemsTheSame(oldItem: LocationEntity, newItem: LocationEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LocationEntity, newItem: LocationEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

}
