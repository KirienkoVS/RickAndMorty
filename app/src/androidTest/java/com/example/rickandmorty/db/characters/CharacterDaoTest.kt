package com.example.rickandmorty.db.characters

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.rickandmorty.data.datasource.db.dao.CharacterDao
import com.example.rickandmorty.data.datasource.db.AppDatabase
import com.example.rickandmorty.data.datasource.db.entity.CharacterData
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@SmallTest
@HiltAndroidTest
@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_database")
    lateinit var database: AppDatabase
    private lateinit var characterDao: CharacterDao
    private lateinit var characterData: CharacterData

    @Before
    fun setUp() {
        hiltRule.inject()
        characterDao = database.characterDao()
        characterData = CharacterData(
                id = 1, name = "Rick", species = "Human", status = "Alive", gender = "Male", image = "",
                type = "", created = "", originName = "Earth", locationName = "Earth", episode = emptyList()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertCharacters() = runTest {
        characterDao.insertCharacters(listOf(characterData))
        val character = characterDao.getCharacterDetails(1)

        assertThat(character).isEqualTo(characterData)
    }

    @Test
    fun clearCharacters() = runTest {
        characterDao.insertCharacters(listOf(characterData))
        characterDao.clearCharacters()
        val character = characterDao.getCharacterDetails(1)

        assertThat(character).isNull()
    }

}
