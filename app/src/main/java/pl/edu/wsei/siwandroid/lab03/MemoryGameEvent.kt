package pl.edu.wsei.siwandroid.lab03

data class MemoryGameEvent(
    val tiles: List<Tile>,
    val state: GameStates
)