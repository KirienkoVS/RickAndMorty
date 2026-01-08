package com.example.rickandmorty.ui.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rickandmorty.R
import com.example.rickandmorty.domain.model.CharacterEntity
import com.example.rickandmorty.ui.view.CharactersFragmentDirections

class CharacterPagingAdapter: PagingDataAdapter<CharacterEntity, RecyclerView.ViewHolder>(CHARACTER_COMPARATOR) {

    class CharacterViewHolder(view: View): RecyclerView.ViewHolder(view) {
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
        return CharacterViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.character_item, parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { character ->
            (holder as CharacterViewHolder).bind(character)

            holder.itemView.setOnClickListener { view ->
                val action = CharactersFragmentDirections.actionCharactersPageToCharacterDetailsFragment(characterID = character.id)
                view.findNavController().navigate(action)
            }
        }

    }

    companion object {
        private val CHARACTER_COMPARATOR = object : DiffUtil.ItemCallback<CharacterEntity>() {
            override fun areItemsTheSame(oldItem: CharacterEntity, newItem: CharacterEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CharacterEntity, newItem: CharacterEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

}
