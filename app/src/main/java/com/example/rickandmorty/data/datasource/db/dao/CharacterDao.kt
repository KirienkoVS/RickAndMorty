package com.example.rickandmorty.data.datasource.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rickandmorty.data.datasource.db.entity.CharacterData

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterData>)

    @Query("SELECT * FROM characters WHERE " +
            "(name LIKE :name OR :name IS NULL) AND " +
            "(species LIKE :species OR :species IS NULL) AND " +
            "(status LIKE :status OR :status IS NULL) AND " +
            "(gender LIKE :gender OR :gender IS NULL) AND " +
            "(type LIKE :type OR :type IS NULL)")
    fun charactersByFilter(
        name: String?,
        species: String?,
        status: String?,
        gender: String?,
        type: String?
    ): PagingSource<Int, CharacterData>

    @Query("SELECT * FROM characters WHERE id = :id")
    fun getCharacterDetails(id: Int): CharacterData

    @Query("SELECT * FROM characters WHERE id IN (:id)")
    fun getLocationOrEpisodeCharacters(id: List<Int>): List<CharacterData>

    @Query("DELETE FROM characters")
    suspend fun clearCharacters()

    @Query("SELECT * FROM characters WHERE " +
            "(name LIKE :query OR :query IS NULL) OR " +
            "(species LIKE :query OR :query IS NULL) OR " +
            "(status LIKE :query OR :query IS NULL) OR " +
            "(gender LIKE :query OR :query IS NULL) OR " +
            "(type LIKE :query OR :query IS NULL)")
    fun charactersBySearch(
        query: String?
    ): PagingSource<Int, CharacterData>

}
