fun main() {
    solveDay02Part(
        "Part01: Which games are playable with given amount of cubes",
        "example_input",
        "example_result",
        "input",
        getIDValueOrZeroForGameLine
    )
    solveDay02Part(
        "Part02: What is the power of the minimal needed cube number per game",
        "example_input2",
        "example_result2",
        "input",
        getPowerOfMinimalCubes
    )
}

fun solveDay02Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    lineSolverFunction: (String) -> Int
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 2)
    val exampleResult = readInput(exampleResultFileName, 2)
    val calculatedExampleResult = exampleInput.sumOf(lineSolverFunction)
    check(calculatedExampleResult == exampleResult.first().toInt())
    println("Calculated example is right! Result: $calculatedExampleResult")

    println("Calculating result for given input file $challengeInputFileName with solver function")
    val challengeInput = readInput(challengeInputFileName, 2)
    val challengeResult = challengeInput.sumOf(lineSolverFunction)
    println("Calculated challenge result: $challengeResult")
}

val getIDValueOrZeroForGameLine: (String) -> Int = { line ->
    val game = createGameFromLine(line)
    if (game.isGamePossibleWithCubes(
            listOf(
                Pair(12, CubeColor.RED),
                Pair(13, CubeColor.GREEN),
                Pair(14, CubeColor.BLUE)
            )
        )
    ) {
        game.id
    } else {
        0
    }
}

val getPowerOfMinimalCubes: (String) -> Int = { line ->
    createGameFromLine(line).getPowerOfMinimalCubes()
}

fun createGameFromLine(gameSpecificationLine: String): Game {
    val game = Game(Regex("Game (\\d*)").find(gameSpecificationLine)!!.groups[1]!!.value.toInt())
    gameSpecificationLine.split(":")[1].split(";").forEach { revelationString ->
        val revelationSteps = mutableListOf<Pair<Int, CubeColor>>()
        revelationString.split(",").forEach {
            val splitRevelation = it.trim().split(" ")
            revelationSteps.add(Pair(splitRevelation[0].toInt(), CubeColor.valueOf(splitRevelation[1].uppercase())))
        }
        game.addRevelation(revelationSteps)
    }
    return game
}

data class Game(val id: Int) {
    private val gameRevelations = mutableListOf<List<Pair<Int, CubeColor>>>()
    fun addRevelation(amountColorCube: List<Pair<Int, CubeColor>>) {
        this.gameRevelations.add(amountColorCube)
    }

    fun isGamePossibleWithCubes(givenAmountColorCubes: List<Pair<Int, CubeColor>>): Boolean {
        val minRequiredCubes = gameRevelations.flatten().groupingBy { it.second }
            .aggregate { _, accumulator: Int?, element, first ->
                if (first || accumulator == null) element.first
                else maxOf(accumulator, element.first)
            }

        return givenAmountColorCubes.all { (amount, color) ->
            amount >= (minRequiredCubes[color] ?: 0)
        }
    }

    fun getPowerOfMinimalCubes(): Int {
        val maxValuesByColor = gameRevelations
            .flatten()
            .groupBy { it.second }
            .mapValues { (_, pairs) -> pairs.maxOf { it.first } }

        return CubeColor.entries.map { maxValuesByColor[it]!! }.reduce { acc, value -> acc * value }
    }
}

enum class CubeColor {
    RED, GREEN, BLUE
}