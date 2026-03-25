package pl.edu.wsei.siwandroid.lab03

import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import pl.edu.wsei.siwandroid.R

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int,
    // DODAJEMY TEN PARAMETR: pozwoli przekazać stare ikony po obrocie
    private val savedResources: IntArray? = null
) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()
    private val icons: List<Int> = listOf(
        R.drawable.baseline_rocket_launch_24,
        R.drawable.baseline_audiotrack_24,
        R.drawable.baseline_money_24,
        R.drawable.baseline_mood_24,
        R.drawable.baseline_monetization_on_24,
        R.drawable.baseline_mouse_24,
        R.drawable.outline_adb_24,
        R.drawable.outline_air_24,
        R.drawable.outline_avocado_bean_24,
        R.drawable.outline_add_2_24,
        R.drawable.outline_1k_24,
        R.drawable.outline_360_24,
        R.drawable.outline_accessibility_24,
        R.drawable.outline_accessible_forward_24,
        R.drawable.outline_airline_seat_recline_normal_24,
        R.drawable.outline_airport_shuttle_24,
        R.drawable.outline_allergy_24,
        R.drawable.outline_barefoot_24,
    )

    private val deckResource: Int = R.drawable.outline_borg_24
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = {}
    private val matchedPair: java.util.Stack<Tile> = java.util.Stack()

    // Logika musi wiedzieć, ile par już odgadnięto (jeśli przywracamy stan)
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)

    init {
        val totalTiles = cols * rows
        val numPairs = totalTiles / 2

        // DECYZJA: albo losujemy nowe, albo bierzemy stare z savedResources
        val shuffledIcons = mutableListOf<Int>()

        if (savedResources != null && savedResources.size == totalTiles) {
            // Przywracamy dokładnie tę samą kolejność ikon co przed obrotem
            shuffledIcons.addAll(savedResources.toList())
        } else {
            // Pierwsze uruchomienie - losujemy
            val subList = icons.take(numPairs)
            shuffledIcons.addAll(subList)
            shuffledIcons.addAll(subList)
            shuffledIcons.shuffle()
        }

        gridLayout.columnCount = cols
        gridLayout.rowCount = rows

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val btn = ImageButton(gridLayout.context).also {
                    it.tag = "${row}x${col}"
                    val params = GridLayout.LayoutParams()
                    params.width = 0
                    params.height = 0
                    params.setGravity(android.view.Gravity.CENTER)
                    params.columnSpec = GridLayout.spec(col, 1, 1f)
                    params.rowSpec = GridLayout.spec(row, 1, 1f)
                    it.layoutParams = params
                }

                if (shuffledIcons.isNotEmpty()) {
                    val icon = shuffledIcons.removeAt(0)
                    addTile(btn, icon)
                }
                gridLayout.addView(btn)
            }
        }
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }

    private fun onClickTile(v: View) {
        val tile = tiles[v.tag] ?: return
        if (tile.revealed) return

        matchedPair.push(tile)
        val matchResult = logic.process { tile.tileResource }

        onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))

        if (matchResult != GameStates.Matching) {
            matchedPair.clear()
        }
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    // --- METODY DO STANU ---

    fun getState(): IntArray {
        val state = IntArray(tiles.size)
        var index = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val tile = tiles["${row}x${col}"]
                // Jeśli karta jest odkryta, zapisujemy jej ID, jeśli nie - zapisujemy -1
                state[index] = if (tile?.revealed == true) tile.tileResource else -1
                index++
            }
        }
        return state
    }

    fun setState(state: IntArray) {
        var index = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val tile = tiles["${row}x${col}"]
                val savedIcon = state[index]
                if (savedIcon != -1) {
                    tile?.revealed = true
                    tile?.removeOnClickListener()
                }
                index++
            }
        }
    }

    fun getAllTilesResources(): IntArray {
        val resources = IntArray(tiles.size)
        var index = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                resources[index] = tiles["${row}x${col}"]?.tileResource ?: -1
                index++
            }
        }
        return resources
    }
}