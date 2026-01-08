package com.example.rickandmorty.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rickandmorty.R
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.ui.view.LocationDetailsFragmentDirections

class LocationDetailsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var residentsList = listOf<CharacterEntity>()

    class LocationResidentsViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.character_name)
        private val species: TextView = view.findViewById(R.id.character_species)
        private val status: TextView = view.findViewById(R.id.character_status)
        private val gender: TextView = view.findViewById(R.id.character_gender)
        private val image: ImageView = view.findViewById(R.id.character_imageview)

        fun bind(resident: CharacterEntity) {
            name.text = resident.name
            species.text = resident.species
            status.text = resident.status
            gender.text = resident.gender
            Glide.with(image).load(resident.image).into(image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LocationResidentsViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.character_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val resident = residentsList[position]
        (holder as LocationResidentsViewHolder).bind(resident)

        holder.itemView.setOnClickListener { view ->
            val residentID = resident.id
            val action = LocationDetailsFragmentDirections.actionLocationDetailsFragmentToCharacterDetailsFragment(characterID = residentID)
            view.findNavController().navigate(action)
        }
    }

    override fun getItemCount() = residentsList.size
}
