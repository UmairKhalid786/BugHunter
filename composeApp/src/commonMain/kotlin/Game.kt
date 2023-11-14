import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

object Game {
    private var job: Job? = null

    var gameStartTime = 0L
        private set

    private val players = mutableListOf<Player>()

    private val _gameState = MutableStateFlow<GameState>(GameState.None)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    var currentPlayer: Player? = players.firstOrNull()
        private set

    fun startGame(player: Player, gameMode: GameMode = GameMode.SinglePlayer) {
        job?.cancel()

        when (gameMode) {
            is GameMode.SinglePlayer -> {
                players.add(player)
            }
        }

        gameStartTime = Clock.System.now().toEpochMilliseconds()
        selectPlayer(player)

        job = CoroutineScope(Dispatchers.Default).launch {
            delay(1.minutes)
            gameOver()
        }

        _gameState.value = GameState.Started(player)
    }

    private fun gameOver() {
        gameStartTime = 0L
        job?.cancel()
        currentPlayer?.let { _gameState.value = GameState.Over(it) }
    }

    fun stopGame() {
        gameStartTime = 0L
        job?.cancel()
        _gameState.value = GameState.Stopped
    }

    fun addScore(player: Player, points: Int) {
        player.addScore(points)
        _gameState.value = GameState.ScoreUpdate(player)
    }

    fun getPlayers() = players.toList()

    fun selectPlayer(player: Player) {
        currentPlayer = player
    }

    fun isGameStarted() = gameStartTime != 0L
}

data class Player(val name: String) {
    var score = 0
        private set

    fun addScore(points: Int) {
        score += points
    }
}

sealed class GameMode {
    data object SinglePlayer : GameMode()
}

sealed class GameState {
    data object None : GameState()
    data object Stopped : GameState()

    data class ScoreUpdate(val player: Player) : GameState()
    data class PlayerSelected(val player: Player) : GameState()
    data class Over(val player: Player) : GameState()
    data class Started(val player: Player) : GameState()
}
