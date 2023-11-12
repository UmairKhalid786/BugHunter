import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun App() {
    CompositionLocalProvider(
        LocalContentAlpha provides ContentAlpha.medium, LocalContentColor provides Color.White
    ) {
        MaterialTheme(
            colors = MaterialTheme.colors.copy(
                primary = Color(0xFFD6D58E),
                onPrimary = Color(0xFF042940),
                surface = Color(0xFF042940),
                secondary = Color(0xFF9FC131),
                onSecondary = Color(0xFFDBF227),
            )
        ) {
            var ballonOne by remember { mutableStateOf(false) }
            var ballonTwo by remember { mutableStateOf(false) }
            var ballonThree by remember { mutableStateOf(false) }

            val state = Game.gameState.collectAsState().value
            val isStopped =
                state is GameState.Over || state is GameState.Stopped || state is GameState.None

            Column(Modifier.fillMaxSize().background(MaterialTheme.colors.surface.copy(0.9f))) {
                AppBar()
                var size by remember { mutableStateOf(IntSize(100, 100)) }
                Box(modifier = Modifier.fillMaxSize().weight(1f).onGloballyPositioned {
                    size = it.size
                }) {
                    if (isStopped.not()) {
                        if (ballonOne) {
                            TearDrop(
                                modifier = Modifier.size(40.dp),
                                parent = -size.height.toFloat(),
                                width = size.width.toFloat()
                            )
                        }
                        if (ballonTwo) {
                            TearDrop(
                                modifier = Modifier.size(40.dp),
                                parent = -size.height.toFloat(),
                                width = size.width.toFloat()
                            )
                        }
                        if (ballonThree) {
                            TearDrop(
                                modifier = Modifier.size(40.dp),
                                parent = -size.height.toFloat(),
                                width = size.width.toFloat()
                            )
                        }
                    }
                }
                AppBottomBar(state)
                LaunchedEffect(isStopped.not()) {
                    launch {
                        while (isStopped.not()) {
                            ballonOne = true
                            delay(1000)
                            launch {
                                delay(1000)
                                ballonOne = false
                            }
                            ballonTwo = true
                            delay(1000)
                            launch {
                                delay(1000)
                                ballonTwo = false
                            }
                            ballonThree = true
                            delay(1000)
                            launch {
                                delay(1000)
                                ballonThree = false
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomBar(state: GameState = GameState.None) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        when (state) {
            is GameState.None -> {
                Button(onClick = {
                    Game.startGame(Player("Player 1"))
                }) {
                    Text("Start Game")
                }
            }

            is GameState.ScoreUpdate -> {
                Button(onClick = {
                    Game.stopGame()
                }) {
                    Text("Stop")
                }
                val player = state.player
                Text("Player: ${player.name} Score: ${player.score}")
            }

            is GameState.Over -> {
                Button(onClick = {
                    Game.startGame(Player("Player 1"))
                }) {
                    Text("Restart Game")
                }
                val player = state.player
                Text("Player: ${player.name} Score: ${player.score}")
            }

            is GameState.Stopped -> {
                Button(onClick = {
                    Game.startGame(Player("Player 1"))
                }) {
                    Text("Restart Game")
                }
            }

            is GameState.Started -> {
                Button(onClick = {
                    Game.stopGame()
                }) {
                    Text("Stop")
                }
                val player = state.player
                Text("Player: ${player.name} Score: ${player.score}")
            }

            else -> {
                Button(onClick = {
                    Game.startGame(Player("Player 1"))
                }) {
                    Text("Start Game")
                }
            }
        }
    }
}

@Composable
fun AppBar() {
    Row(
        modifier = Modifier.fillMaxWidth().wrapContentHeight().background(MaterialTheme.colors.surface),
    ) {
        Text(
            text = "Balloon Hunter",
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
    }
}

fun createPath(sides: Int, radius: Float, cx: Float, cy: Float): Path {
    val path = Path()
    val angle = 2.0 * PI / sides

    val initialAngle = 145f

    path.moveTo(
        cx + (radius * cos(initialAngle)), cy + (radius * sin(initialAngle))
    )
    for (i in 1 until sides) {
        path.lineTo(
            cx + (radius * cos(angle * i + initialAngle)).toFloat(),
            cy + (radius * sin(angle * i + initialAngle)).toFloat()
        )
    }
    path.close()
    return path
}

@Composable
fun TearDrop(modifier: Modifier = Modifier, parent: Float = -2000f, width: Float = 0f) {
    var background by remember { mutableStateOf(Color(0xFFDBF227)) }

    val x = remember { (0..(width * 0.9f).toInt()).random() }
    val yAnimation = remember { Animatable(2000f) }

    val isTouched = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        yAnimation.animateTo(
            parent, animationSpec = spring(stiffness = 1f)
        )
    }

    AnimatedVisibility(isTouched.value.not(), exit = shrinkOut()) {
        Canvas(modifier = modifier.offset {
            IntOffset(x, yAnimation.value.toInt())
        }.clickable {
            isTouched.value = true
            background = Color.Red
            Game.currentPlayer?.let { Game.addScore(it, 1) }
        }, onDraw = {
            drawOval(
                color = background, topLeft = Offset(0f, 0f), size = Size(size.width, size.height)
            )
            drawPath(
                createPath(
                    3, size.width / 7, size.width / 2, size.height + (size.width / 10)
                ), background
            )
        })
    }
}
