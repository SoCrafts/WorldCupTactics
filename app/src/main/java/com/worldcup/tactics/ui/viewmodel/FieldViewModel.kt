package com.worldcup.tactics.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldcup.tactics.domain.model.FormationPlayer
import com.worldcup.tactics.domain.model.Player
import com.worldcup.tactics.domain.model.PositionGroup
import com.worldcup.tactics.domain.model.positionGroup
import com.worldcup.tactics.domain.repository.FormationRepository
import com.worldcup.tactics.domain.usecase.GetPlayersUseCase
import com.worldcup.tactics.domain.usecase.GetTeamsUseCase
import com.worldcup.tactics.domain.usecase.LoadFormationUseCase
import com.worldcup.tactics.domain.usecase.SaveFormationUseCase
import com.worldcup.tactics.ui.state.FieldUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FieldViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTeamsUseCase: GetTeamsUseCase,
    private val getPlayersUseCase: GetPlayersUseCase,
    private val loadFormationUseCase: LoadFormationUseCase,
    private val saveFormationUseCase: SaveFormationUseCase,
    private val formationRepository: FormationRepository
) : ViewModel() {

    private val teamId: Int = checkNotNull(savedStateHandle["teamId"])

    private val _uiState = MutableStateFlow(FieldUiState())
    val uiState: StateFlow<FieldUiState> = _uiState.asStateFlow()

    // Memorizza l'intera rosa della squadra (tutti i giocatori)
    private val _fullSquad = MutableStateFlow<List<Player>>(emptyList())

    // Temporary storage for player coordinate when drag starts
    private var dragStartPos: Pair<Float, Float>? = null

    init {
        loadPlayers()
    }

    fun loadPlayers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val teamName = getTeamsUseCase()
                .getOrNull()
                ?.find { it.id == teamId }
                ?.name
                .orEmpty()



            getPlayersUseCase(teamId)
                .onSuccess { players ->
                    _fullSquad.value = players
                    val saved = loadFormationUseCase(teamId)
                    val savedLabel = formationRepository.getFormationLabel(teamId)
                    val formationPlayers = if (saved != null && saved.isNotEmpty()) {
                        mergeSavedWithCurrent(saved, players)
                    } else {
                        applyFormation(players, "4-4-2")
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            teamName = teamName,
                            players = formationPlayers,
                            benchPlayers = deriveBench(players, formationPlayers),
                            formationLabel = savedLabel ?: "4-4-2",
                            isSaved = saved != null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load players"
                        )
                    }
                }
        }
    }

    fun selectFormation(name: String) {
        if (name == "Custom") return

        val squad = _fullSquad.value
        if (squad.isEmpty()) return

        val formationPlayers = applyFormation(squad, name)
        _uiState.update {
            it.copy(
                players = formationPlayers,
                benchPlayers = deriveBench(squad, formationPlayers),
                formationLabel = name,
                isSaved = false,
                selectedPlayerId = null
            )
        }
    }

    fun startDragging(playerId: Int) {
        val player = _uiState.value.players.find { it.player.id == playerId }
        if (player != null) {
            dragStartPos = Pair(player.xNorm, player.yNorm)
        }
    }

    fun movePlayer(playerId: Int, xNorm: Float, yNorm: Float) {
        _uiState.update { state ->
            state.copy(
                players = state.players.map { fp ->
                    if (fp.player.id == playerId) {
                        fp.copy(
                            xNorm = xNorm.coerceIn(0.05f, 0.95f),
                            yNorm = yNorm.coerceIn(0.05f, 0.95f)
                        )
                    } else {
                        fp
                    }
                },
                isSaved = false
            )
        }
    }

    fun endDragging(playerId: Int, fieldWidthPx: Float, fieldHeightPx: Float, thresholdPx: Float) {
        val startPos = dragStartPos ?: return
        dragStartPos = null

        val state = _uiState.value
        val players = state.players
        val dragged = players.find { it.player.id == playerId } ?: return

        // Find overlapping player
        val overlappingPlayer = players.firstOrNull { fp ->
            if (fp.player.id == playerId) return@firstOrNull false
            val dx = (fp.xNorm - dragged.xNorm) * fieldWidthPx
            val dy = (fp.yNorm - dragged.yNorm) * fieldHeightPx
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
            dist < thresholdPx
        }



        if (overlappingPlayer != null) {
            // Swap positions
            _uiState.update { s ->
                s.copy(
                    players = s.players.map { fp ->
                        when (fp.player.id) {
                            playerId -> fp.copy(xNorm = overlappingPlayer.xNorm, yNorm = overlappingPlayer.yNorm)
                            overlappingPlayer.player.id -> fp.copy(xNorm = startPos.first, yNorm = startPos.second)
                            else -> fp
                        }
                    },
                    formationLabel = "Custom",
                    isSaved = false
                )
            }
        } else {
            if (dragged.xNorm != startPos.first || dragged.yNorm != startPos.second) {
                _uiState.update { s ->
                    s.copy(
                        formationLabel = "Custom",
                        isSaved = false
                    )
                }
            }
        }
    }

    fun selectPlayer(playerId: Int?) {
        _uiState.update { state ->
            val newId = if (playerId != null && playerId == state.selectedPlayerId) null else playerId
            state.copy(selectedPlayerId = newId)
        }
    }

    fun substitutePlayer(fieldPlayerId: Int, benchPlayer: Player) {
        _uiState.update { state ->
            val target = state.players.find { it.player.id == fieldPlayerId } ?: return@update state
            val newPlayers = state.players.map { fp ->
                if (fp.player.id == fieldPlayerId)
                    fp.copy(player = benchPlayer)   // xNorm/yNorm preserved
                else fp
            }
            state.copy(
                players = newPlayers,
                benchPlayers = deriveBench(_fullSquad.value, newPlayers),
                selectedPlayerId = null,
                isSaved = false
            )
        }
    }

    fun resetFormation() {
        val currentPlayers = _uiState.value.players.map { it.player }
        if (currentPlayers.isEmpty()) return
        val currentLabel = _uiState.value.formationLabel.takeIf { it != "Custom" } ?: "4-4-2"
        val squad = _fullSquad.value
        if (squad.isEmpty()) return

        val newFormationPlayers = applyFormation(squad, currentLabel)
        _uiState.update {
            it.copy(
                players = newFormationPlayers,
                benchPlayers = deriveBench(squad, newFormationPlayers),
                formationLabel = currentLabel,
                selectedPlayerId = null,
                isSaved = false
            )
        }
    }

    fun saveFormation() {
        viewModelScope.launch {
            val state = _uiState.value
            saveFormationUseCase(teamId, state.players)
            formationRepository.saveFormationLabel(teamId, state.formationLabel)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────────

    private fun deriveBench(
        fullSquad: List<Player>,
        onPitch: List<FormationPlayer>
    ): List<Player> {
        val onPitchIds = onPitch.map { it.player.id }.toSet()
        return fullSquad.filter { it.id !in onPitchIds }
    }

    private data class FormationCounts(val def: Int, val mid: Int, val fwd: Int)

    private fun getFormationCounts(name: String): FormationCounts = when (name) {
        "4-4-2" -> FormationCounts(4, 4, 2)
        "4-3-3" -> FormationCounts(4, 3, 3)
        "4-2-3-1" -> FormationCounts(4, 5, 1)
        "3-5-2" -> FormationCounts(3, 5, 2)
        "3-4-3" -> FormationCounts(3, 4, 3)
        "5-3-2" -> FormationCounts(5, 3, 2)
        "5-4-1" -> FormationCounts(5, 4, 1)
        "4-5-1" -> FormationCounts(4, 5, 1)
        else -> FormationCounts(4, 4, 2)
    }

    private fun applyFormation(
        squad: List<Player>,
        formationName: String
    ): List<FormationPlayer> {

        val counts = getFormationCounts(formationName)
        val selectedPlayers = selectStarting11(
            squad,
            counts.def,
            counts.mid,
            counts.fwd
        )

        val hasGoalkeeper = selectedPlayers.any { it.positionGroup() == PositionGroup.GK }

        val coords = if (hasGoalkeeper) {
            getFormationCoords(formationName)
        } else {
            // Skip the goalkeeper coordinate
            getFormationCoords(formationName).drop(1)
        }.take(selectedPlayers.size)

        val sortedPlayers = selectedPlayers.sortedWith(compareBy {
            when (it.positionGroup()) {
                PositionGroup.GK -> 0
                PositionGroup.DEF -> 1
                PositionGroup.MID -> 2
                PositionGroup.FWD -> 3
                else -> 4
            }
        })

        return sortedPlayers.mapIndexed { index, player ->
            val coord = coords.getOrElse(index) { 0.5f to 0.5f }
            FormationPlayer(
                player = player,
                xNorm = coord.first,
                yNorm = coord.second
            )
        }
    }

    private fun mergeSavedWithCurrent(
        saved: List<FormationPlayer>,
        current: List<Player>
    ): List<FormationPlayer> {
        val currentById = current.associateBy { it.id }
        val merged = saved.mapNotNull { savedPlayer ->
            currentById[savedPlayer.player.id]?.let { latest ->
                savedPlayer.copy(player = latest)
            }
        }.toMutableList()

        // Pad only up to the actual squad size, never beyond it.
        val maxSize = minOf(11, current.size)
        if (merged.size < maxSize) {
            val unused = current.filter { player -> merged.none { it.player.id == player.id } }
            var unusedIndex = 0
            while (merged.size < maxSize && unusedIndex < unused.size) {
                val player = unused[unusedIndex++]
                merged.add(
                    FormationPlayer(
                        player = player,
                        xNorm = 0.5f,
                        yNorm = 0.5f
                    )
                )
            }
        }
        return merged.take(maxSize)
    }

    private fun selectStarting11(
        squad: List<Player>,
        defCount: Int,
        midCount: Int,
        fwdCount: Int
    ): List<Player> {

        if (squad.isEmpty()) return emptyList()

        val selected = mutableListOf<Player>()
        val remaining = squad.toMutableList()

        // ─────────────────────────────────────────────
        // 1. Goalkeeper (if exists)
        // ─────────────────────────────────────────────
        val gk = remaining
            .filter { it.positionGroup() == PositionGroup.GK }
            .maxByOrNull { it.number ?: 0 }

        if (gk != null) {
            selected.add(gk)
            remaining.remove(gk)
        }

        // ─────────────────────────────────────────────
        // 2. Helper: pick best players per position
        // (we don't have rating in domain yet, so we
        //  approximate using number + CSV fallback)
        // ─────────────────────────────────────────────
        fun pick(group: PositionGroup, count: Int) {
            val candidates = remaining
                .filter { it.positionGroup() == group }
                .sortedWith(
                    compareByDescending<Player> { it.number ?: 0 } // proxy strength
                        .thenBy { it.name }
                )
                .take(count)

            selected.addAll(candidates)
            remaining.removeAll(candidates.toSet())
        }

        // ─────────────────────────────────────────────
        // 3. Build formation
        // ─────────────────────────────────────────────
        pick(PositionGroup.DEF, defCount)
        pick(PositionGroup.MID, midCount)
        pick(PositionGroup.FWD, fwdCount)

        // ─────────────────────────────────────────────
        // 4. Fill remaining slots
        // ─────────────────────────────────────────────
        while (selected.size < minOf(11, squad.size) && remaining.isNotEmpty()) {
            selected.add(remaining.removeAt(0))
        }

        return selected
    }


    private fun getFormationCoords(name: String): List<Pair<Float, Float>> {
        val gk = Pair(0.5f, 0.85f)
        val outfields = when (name) {
            "4-4-2" -> listOf(
                Pair(0.15f, 0.65f), Pair(0.38f, 0.68f), Pair(0.62f, 0.68f), Pair(0.85f, 0.65f),
                Pair(0.15f, 0.45f), Pair(0.38f, 0.48f), Pair(0.62f, 0.48f), Pair(0.85f, 0.45f),
                Pair(0.35f, 0.22f), Pair(0.65f, 0.22f)
            )
            "4-3-3" -> listOf(
                Pair(0.15f, 0.65f), Pair(0.38f, 0.68f), Pair(0.62f, 0.68f), Pair(0.85f, 0.65f),
                Pair(0.25f, 0.48f), Pair(0.5f, 0.53f), Pair(0.75f, 0.48f),
                Pair(0.2f, 0.22f), Pair(0.5f, 0.18f), Pair(0.8f, 0.22f)
            )
            "4-2-3-1" -> listOf(
                Pair(0.15f, 0.65f), Pair(0.38f, 0.68f), Pair(0.62f, 0.68f), Pair(0.85f, 0.65f),
                Pair(0.35f, 0.52f), Pair(0.65f, 0.52f),
                Pair(0.2f, 0.35f), Pair(0.5f, 0.33f), Pair(0.8f, 0.35f),
                Pair(0.5f, 0.18f)
            )
            "3-5-2" -> listOf(
                Pair(0.25f, 0.68f), Pair(0.5f, 0.70f), Pair(0.75f, 0.68f),
                Pair(0.12f, 0.48f), Pair(0.32f, 0.48f), Pair(0.5f, 0.53f), Pair(0.68f, 0.48f), Pair(0.88f, 0.48f),
                Pair(0.35f, 0.22f), Pair(0.65f, 0.22f)
            )
            "3-4-3" -> listOf(
                Pair(0.25f, 0.68f), Pair(0.5f, 0.70f), Pair(0.75f, 0.68f),
                Pair(0.15f, 0.48f), Pair(0.38f, 0.50f), Pair(0.62f, 0.50f), Pair(0.85f, 0.48f),
                Pair(0.2f, 0.22f), Pair(0.5f, 0.18f), Pair(0.8f, 0.22f)
            )
            "5-3-2" -> listOf(
                Pair(0.12f, 0.62f), Pair(0.3f, 0.68f), Pair(0.5f, 0.70f), Pair(0.7f, 0.68f), Pair(0.88f, 0.62f),
                Pair(0.25f, 0.48f), Pair(0.5f, 0.50f), Pair(0.75f, 0.48f),
                Pair(0.35f, 0.22f), Pair(0.65f, 0.22f)
            )
            "5-4-1" -> listOf(
                Pair(0.12f, 0.62f), Pair(0.3f, 0.68f), Pair(0.5f, 0.70f), Pair(0.7f, 0.68f), Pair(0.88f, 0.62f),
                Pair(0.15f, 0.45f), Pair(0.38f, 0.48f), Pair(0.62f, 0.48f), Pair(0.85f, 0.45f),
                Pair(0.5f, 0.20f)
            )
            "4-5-1" -> listOf(
                Pair(0.15f, 0.65f), Pair(0.38f, 0.68f), Pair(0.62f, 0.68f), Pair(0.85f, 0.65f),
                Pair(0.15f, 0.42f), Pair(0.35f, 0.46f), Pair(0.5f, 0.50f), Pair(0.65f, 0.46f), Pair(0.85f, 0.42f),
                Pair(0.5f, 0.20f)
            )
            else -> listOf(
                Pair(0.15f, 0.65f), Pair(0.38f, 0.68f), Pair(0.62f, 0.68f), Pair(0.85f, 0.65f),
                Pair(0.15f, 0.45f), Pair(0.38f, 0.48f), Pair(0.62f, 0.48f), Pair(0.85f, 0.45f),
                Pair(0.35f, 0.22f), Pair(0.65f, 0.22f)
            )
        }
        return listOf(gk) + outfields
    }
}