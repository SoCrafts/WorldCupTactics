package com.worldcup.tactics.data.local

import android.content.Context
import com.worldcup.tactics.R
import com.worldcup.tactics.data.mapper.CsvPlayerMapper
import com.worldcup.tactics.domain.model.Player
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvPlayerDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Returns all players belonging to [teamName] from the compact CSV.
     *
     * The compact CSV has exactly one row per unique player (already deduplicated),
     * so no further deduplication is required here.
     *
     * Compact CSV column layout (9 columns):
     *   0 player_id | 1 player_name | 2 team | 3 jersey_number | 4 position
     *   5 nationality | 6 height_cm | 7 weight_kg | 8 club_name
     */
    fun getPlayers(teamName: String): List<Player> {

        val input = context.resources.openRawResource(R.raw.players)

        return input.bufferedReader().useLines { lines ->

            lines
                .drop(1) // header
                .mapNotNull { line ->

                    val columns = line.split(",")

                    // Ignore malformed rows (need at least 9 columns)
                    if (columns.size < 9) {
                        null
                    } else {

                        // col[2] is the national team name
                        if (!columns[2].equals(teamName, ignoreCase = true)) {
                            null
                        } else {
                            CsvPlayerMapper.toDomain(columns)
                        }
                    }
                }
                .toList()
        }
    }

    /**
     * Returns a map of lowercased player name → [Player] for every player in the CSV,
     * regardless of team. Used by [com.worldcup.tactics.data.repository.PlayerRepositoryImpl]
     * to enrich API players that are missing a jersey number.
     */
    fun getAllPlayersByName(): Map<String, Player> {

        val input = context.resources.openRawResource(R.raw.players)

        return input.bufferedReader().useLines { lines ->

            lines
                .drop(1) // header
                .mapNotNull { line ->

                    val columns = line.split(",")

                    if (columns.size < 9) null
                    else CsvPlayerMapper.toDomain(columns)
                }
                .associateBy { it.name.trim().lowercase() }
        }
    }
}