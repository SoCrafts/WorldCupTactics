package com.worldcup.tactics.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.worldcup.tactics.domain.model.FormationPlayer
import com.worldcup.tactics.domain.model.Player
import com.worldcup.tactics.domain.repository.FormationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.formationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "formations"
)

@Serializable
data class SavedFormationPlayer(
    val playerId: Int,
    val name: String,
    val number: Int?,
    val position: String,
    val nationality: String,
    val photoUrl: String?,
    val clubName: String?,
    val xNorm: Float,
    val yNorm: Float,
    val dateOfBirth: String? = null,
    val height: String? = null,
    val weight: String? = null
)

@Singleton
class FormationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FormationRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveFormation(teamId: Int, players: List<FormationPlayer>) {
        val payload = players.map { fp ->
            SavedFormationPlayer(
                playerId = fp.player.id,
                name = fp.player.name,
                number = fp.player.number,
                position = fp.player.position,
                nationality = fp.player.nationality,
                photoUrl = fp.player.photoUrl,
                clubName = fp.player.clubName,
                xNorm = fp.xNorm,
                yNorm = fp.yNorm,
                dateOfBirth = fp.player.dateOfBirth,
                height = fp.player.height,
                weight = fp.player.weight
            )
        }
        context.formationDataStore.edit { prefs ->
            prefs[formationKey(teamId)] = json.encodeToString(payload)
            prefs[labelKey(teamId)] = "Custom"
        }
    }

    override suspend fun loadFormation(teamId: Int): List<FormationPlayer>? {
        val raw = context.formationDataStore.data.first()[formationKey(teamId)] ?: return null
        return runCatching {
            json.decodeFromString<List<SavedFormationPlayer>>(raw).map { saved ->
                FormationPlayer(
                    player = Player(
                        id = saved.playerId,
                        name = saved.name,
                        number = saved.number,
                        position = saved.position,
                        nationality = saved.nationality,
                        photoUrl = saved.photoUrl,
                        clubName = saved.clubName,
                        dateOfBirth = saved.dateOfBirth,
                        height = saved.height,
                        weight = saved.weight
                    ),
                    xNorm = saved.xNorm,
                    yNorm = saved.yNorm
                )
            }
        }.getOrNull()
    }

    override suspend fun getFormationLabel(teamId: Int): String? {
        return context.formationDataStore.data.first()[labelKey(teamId)]
    }

    override suspend fun saveFormationLabel(teamId: Int, label: String) {
        context.formationDataStore.edit { prefs ->
            prefs[labelKey(teamId)] = label
        }
    }

    private fun formationKey(teamId: Int) = stringPreferencesKey("formation_$teamId")
    private fun labelKey(teamId: Int) = stringPreferencesKey("formation_label_$teamId")
}
