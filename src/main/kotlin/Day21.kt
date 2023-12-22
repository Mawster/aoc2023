import kotlin.system.measureTimeMillis

fun main() {
    solveDay21Part(
        "Part01: Calculate reachable garden plots",
        "example_input",
        "example_result",
        "input",
        calculateReachableGardenPlots
    )
}

fun solveDay21Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 21)
    val exampleResult = readInput(exampleResultFileName, 21)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        println("Calculated example Result: $calculatedExampleResult")
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 21)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val calculateReachableGardenPlots: (List<String>) -> Long = { gardenLines: List<String> ->
    val currentReachedGardenPlots = mutableSetOf(getStartingPointFromGardenLines(gardenLines))
    val stepsToMake = 64L

    for (step in 0 until stepsToMake) {
        val startingPoints = currentReachedGardenPlots.toMutableList()
        currentReachedGardenPlots.clear()
        while(startingPoints.isNotEmpty()){
            val currentStartingPoint = startingPoints.removeAt(0)
            val reachableGardenPlots = getReachableGardenPlots(currentStartingPoint, gardenLines)
            currentReachedGardenPlots.addAll(reachableGardenPlots)
        }
    }

    currentReachedGardenPlots.size.toLong()
}

fun getReachableGardenPlots(currentStartingPoint: Point, gardenLines: List<String>): Collection<Point> {
    val maxRowIndex = gardenLines.size - 1
    val maxColumnIndex = gardenLines[0].length - 1

    return mutableSetOf(
        Point(currentStartingPoint.x, currentStartingPoint.y - 1),
        Point(currentStartingPoint.x, currentStartingPoint.y + 1),
        Point(currentStartingPoint.x - 1, currentStartingPoint.y),
        Point(currentStartingPoint.x + 1, currentStartingPoint.y)
    ).filter { point ->
        point.x in 0..maxColumnIndex && point.y in 0..maxRowIndex && gardenLines[point.y][point.x] != '#' }
}

fun getStartingPointFromGardenLines(gardenLines: List<String>): Point {
    var startingPoint: Point? = null
    gardenLines.forEachIndexed { y, gardenLine ->
        gardenLine.forEachIndexed { x, gardenTile ->
            if (gardenTile == 'S') {
                startingPoint = Point(x, y)
            }
        }
    }
    return startingPoint!!
}