import kotlin.system.measureTimeMillis

fun main() {
    solveDay10Part(
        "Part01: Calculate longest path in loop",
        "example_input",
        "example_result",
        "input",
        calculateFarthestPointFromStart
    )
}

fun solveDay10Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

//    println("Checking example with given solver")
//    val exampleInput = readInput(exampleInputFileName, 10)
//    val exampleResult = readInput(exampleResultFileName, 10)
//    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
//    var calculcationTime = measureTimeMillis {
//        check(calculatedExampleResult == exampleResult.first().toLong())
//        println("Calculated example is right! Result: $calculatedExampleResult")
//    }
//    println("Calculation time: $calculcationTime ms")

    val calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 10)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val calculateFarthestPointFromStart: (List<String>) -> Long = { inputLines ->
    val addedTiles = mutableListOf<Tile>()
    val allTiles = mutableListOf<Tile>()
    val graphicalLoop = cloneInput(inputLines)
    val startingPoint = findStartingPoint(inputLines)
    val startTile = Tile(
        startingPoint
    )

    addedTiles.add(startTile)
    allTiles.add(startTile)

    while (addedTiles.groupBy { it.ownPosition }.values.any { it.size < 2 }) {
        val nextTilesToCheck = mutableListOf<Tile>()
        addedTiles.forEach { tile ->
            val connectedTiles = getConnectedTilesBasedOfPoint(inputLines, tile.ownPosition, tile.sourcePoint)
            tile.connectedTiles.addAll(connectedTiles)
            nextTilesToCheck.addAll(connectedTiles)
        }

        addedTiles.clear()
        addedTiles.addAll(nextTilesToCheck)
        allTiles.addAll(nextTilesToCheck)
    }

    val maxDistanceWithFarthestNode = longestPath(startTile)
    val tilesWhoReachFarthestNode = allTiles
        .filter { canReachFarthestNode(it, maxDistanceWithFarthestNode.second) }
        .sortedBy { it.ownPosition.y }


    val filledGraphicalLoop = mutableListOf<String>()
    for (line in tilesWhoReachFarthestNode.groupBy { it.ownPosition.y }) {
        val y = line.key
        val strBuilder = StringBuilder(graphicalLoop[y])
        for (tile in line.value) {
            strBuilder.setCharAt(tile.ownPosition.x, inputLines[tile.ownPosition.y][tile.ownPosition.x])
        }
        filledGraphicalLoop.add(strBuilder.toString())
    }
    filledGraphicalLoop.forEach {println(it)}

    maxDistanceWithFarthestNode.first.toLong()
}

fun canReachFarthestNode(currentNode: Tile, farthestNode: Tile): Boolean {
    val visited = mutableSetOf<Tile>()
    fun dfs(node: Tile): Boolean {
        visited.add(node)
        if (node.ownPosition.x == farthestNode.ownPosition.x && node.ownPosition.y == farthestNode.ownPosition.y) {
            return true
        }
        for (neighbour in node.connectedTiles) {
            if (neighbour !in visited) {
                if (dfs(neighbour)) {
                    return true
                }
            }
        }
        return false
    }
    return dfs(currentNode)
}

fun cloneInput(inputLines: List<String>): List<String> {
    val clonedInput = mutableListOf<String>()
    inputLines.forEach { line ->
        clonedInput.add(line.replace(Regex(".")," "))
    }
    return clonedInput
}

fun longestPath(root: Tile): Pair<Int, Tile> {
    val visited = mutableSetOf<Tile>()
    var maxDistance = 0
    var farthestNode: Tile? = null

    fun dfs(node: Tile, distance: Int) {
        visited.add(node)
        if (distance > maxDistance) {
            maxDistance = distance
            farthestNode = node
        }
        for (neighbour in node.connectedTiles) {
            if (neighbour !in visited) {
                dfs(neighbour, distance + 1)
            }
        }
    }

    // First DFS to find the farthest node from root
    dfs(root, 0)

    return Pair(maxDistance, farthestNode!!)
}

fun getConnectedTilesBasedOfPoint(
    inputLines: List<String>,
    startingPoint: Point,
    sourcePoint: Point? = null
): List<Tile> {
    val connectedTiles = mutableListOf<Tile>()

    if (isPointInBounds(inputLines, startingPoint.x, startingPoint.y - 1) &&
        sourcePoint != Point(startingPoint.x, startingPoint.y - 1) &&
        inputLines[startingPoint.y - 1][startingPoint.x] in listOf(
            ConnectionType.VERTICAL_PIPE.character,
            ConnectionType.SOUTH_WEST_PIPE.character,
            ConnectionType.SOUTH_EAST_PIPE.character
        ) &&
        (inputLines[startingPoint.y][startingPoint.x] in listOf(
            ConnectionType.VERTICAL_PIPE.character,
            ConnectionType.NORTH_EAST_PIPE.character,
            ConnectionType.NORTH_WEST_PIPE.character
        ) || sourcePoint == null)
    ) {
        connectedTiles.add(Tile(Point(startingPoint.x, startingPoint.y - 1), startingPoint))
    }

    if (isPointInBounds(inputLines, startingPoint.x, startingPoint.y + 1) &&
        sourcePoint != Point(startingPoint.x, startingPoint.y+1) &&
        inputLines[startingPoint.y +1 ][startingPoint.x] in listOf(
            ConnectionType.VERTICAL_PIPE.character,
            ConnectionType.NORTH_EAST_PIPE.character,
            ConnectionType.NORTH_WEST_PIPE.character
        ) &&
        (inputLines[startingPoint.y][startingPoint.x] in listOf(
            ConnectionType.VERTICAL_PIPE.character,
            ConnectionType.SOUTH_WEST_PIPE.character,
            ConnectionType.SOUTH_EAST_PIPE.character
        ) || sourcePoint == null)
    ) {
        connectedTiles.add(Tile(Point(startingPoint.x, startingPoint.y + 1), startingPoint))
    }

    if (isPointInBounds(inputLines, startingPoint.x - 1, startingPoint.y) &&
        sourcePoint != Point(startingPoint.x - 1, startingPoint.y) &&
        inputLines[startingPoint.y][startingPoint.x - 1] in listOf(
            ConnectionType.HORIZONTAL_PIPE.character,
            ConnectionType.NORTH_EAST_PIPE.character,
            ConnectionType.SOUTH_EAST_PIPE.character
        ) &&
        (inputLines[startingPoint.y][startingPoint.x] in listOf(
            ConnectionType.HORIZONTAL_PIPE.character,
            ConnectionType.NORTH_WEST_PIPE.character,
            ConnectionType.SOUTH_WEST_PIPE.character
        ) || sourcePoint == null)
    ) {
        connectedTiles.add(Tile(Point(startingPoint.x - 1, startingPoint.y), startingPoint))
    }

    if (isPointInBounds(inputLines, startingPoint.x + 1, startingPoint.y) &&
        sourcePoint != Point(startingPoint.x + 1, startingPoint.y) &&
        inputLines[startingPoint.y][startingPoint.x + 1] in listOf(
            ConnectionType.HORIZONTAL_PIPE.character,
            ConnectionType.NORTH_WEST_PIPE.character,
            ConnectionType.SOUTH_WEST_PIPE.character
        ) &&
        (inputLines[startingPoint.y][startingPoint.x] in listOf(
            ConnectionType.HORIZONTAL_PIPE.character,
            ConnectionType.SOUTH_EAST_PIPE.character,
            ConnectionType.NORTH_EAST_PIPE.character
        ) || sourcePoint == null)
    ) {
        connectedTiles.add(Tile(Point(startingPoint.x + 1, startingPoint.y), startingPoint))
    }
    return connectedTiles
}

fun isPointInBounds(inputLines: List<String>, x: Int, y: Int): Boolean {
    return y >= 0 && y < inputLines.size && x >= 0 && x < inputLines[y].length
}

fun findStartingPoint(inputLines: List<String>): Point {
    for ((y, line) in inputLines.withIndex()) {
        for ((x, character) in line.withIndex()) {
            if (character == ConnectionType.START.character) {
                return Point(x, y)
            }
        }
    }
    throw IllegalStateException("No starting point found")
}

data class Tile(val ownPosition: Point, val sourcePoint: Point? = null) {
    var connectedTiles = mutableListOf<Tile>()
}

enum class ConnectionType(val character: Char) {
    VERTICAL_PIPE('║'),
    HORIZONTAL_PIPE('═'),
    NORTH_EAST_PIPE('╚'),
    NORTH_WEST_PIPE('╝'),
    SOUTH_WEST_PIPE('╗'),
    SOUTH_EAST_PIPE('╔'),
    GROUND('.'),
    START('S')
}