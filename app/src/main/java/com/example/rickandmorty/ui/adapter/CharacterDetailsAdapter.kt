package com.example.rickandmorty.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.R
import com.example.rickandmorty.domain.model.EpisodeEntity
import com.example.rickandmorty.ui.view.CharacterDetailsFragmentDirections

class CharacterDetailsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var episodeList = listOf<EpisodeEntity>()

    class CharacterEpisodeViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val episodeName: TextView = view.findViewById(R.id.episode_name)
        private val episodeNumber: TextView = view.findViewById(R.id.episode_episode)
        private val episodeDate: TextView = view.findViewById(R.id.episode_date)

        fun bind(episode: EpisodeEntity) {
            episodeName.text = episode.name
            episodeNumber.text = episode.episodeNumber
            episodeDate.text = episode.airDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CharacterEpisodeViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.episode_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val episode = episodeList[position]
        (holder as CharacterEpisodeViewHolder).bind(episode)

        holder.itemView.setOnClickListener { view ->
            val episodeID = episode.id
            val action = CharacterDetailsFragmentDirections.actionCharacterDetailsFragmentToEpisodeDetailsFragment(episodeID = episodeID)
            view.findNavController().navigate(action)

        }
    }

    override fun getItemCount() = episodeList.size
}
