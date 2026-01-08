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
import com.example.rickandmorty.ui.view.EpisodeDetailsFragmentDirections

class EpisodeDetailsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var characterList = listOf<CharacterEntity>()

    class EpisodeCharactersViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.character_name)
        private val species: TextView = view.findViewById(R.id.character_species)
        private val status: TextView = view.findViewById(R.id.character_status)
        private val gender: TextView = view.findViewById(R.id.character_gender)
        private val image: ImageView = view.findViewById(R.id.character_imageview)

        fun bind(character: CharacterEntity) {
            name.text = character.name
            species.text = character.species
            status.text = character.status
            gender.text = character.gender
            Glide.with(image).load(character.image).into(image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EpisodeCharactersViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.character_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val character = characterList[position]
        (holder as EpisodeCharactersViewHolder).bind(character)

        holder.itemView.setOnClickListener { view ->
            val characterID = character.id
            val action = EpisodeDetailsFragmentDirections.actionEpisodeDetailsFragmentToCharacterDetailsFragment(characterID = characterID)
            view.findNavController().navigate(action)
        }
    }

    override fun getItemCount() = characterList.size
}
