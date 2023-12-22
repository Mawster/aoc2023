import kotlin.system.measureTimeMillis

fun main() {
    solveDay17Part(
        "Part01: Calculate minimal heat loss moving through the city",
        "example_input",
        "example_result",
        "input",
        calculateMinimalHeatLoss
    )
}

fun solveDay17Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 17)
    val exampleResult = readInput(exampleResultFileName, 17)
//    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
//    var calculcationTime = measureTimeMillis {
//        println("Calculated example Result: $calculatedExampleResult")
//        check(calculatedExampleResult == exampleResult.first().toLong())
//        println("Calculated example is right! Result: $calculatedExampleResult")
//    }
//    println("Calculation time: $calculcationTime ms")

    var calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 17)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

var currentMinimalHeatLoss = Long.MAX_VALUE
var minimalHeatLossToEndForPoint = mutableMapOf<Pair<Point, HitDirection>, Long>()
var shortestPath = mutableListOf<CityMove>()
var highestPoint = Point(0, 0)

val calculateMinimalHeatLoss: (List<String>) -> Long = { cityDefinition: List<String> ->
    currentMinimalHeatLoss = Long.MAX_VALUE
    minimalHeatLossToEndForPoint = mutableMapOf<Pair<Point, HitDirection>, Long>()
    shortestPath = mutableListOf<CityMove>()
    highestPoint = Point(0, 0)
    println("Point to reach is ${Point(cityDefinition[0].length - 1, cityDefinition.size - 1)}")
    calculateHeatLossByStartPoint(cityDefinition, Point(0, 0), mutableListOf())
}

fun calculateHeatLossByStartPoint(
    cityDefinition: List<String>,
    startPoint: Point,
    pathUntilNow: MutableList<CityMove>,
): Long {
    val path = pathUntilNow
    var currentPoint = startPoint
    if (path.isEmpty()) {
        path.add(
            CityMove(
                startPoint,
                HitDirection.EAST,
                0
            )
        )
    }

    val currentHeatLossLevel = path.sumOf { it.heatLoss.toInt() }.toLong()

    if (currentHeatLossLevel > currentMinimalHeatLoss) {
        return Long.MAX_VALUE
    }

    if(currentPoint.y > highestPoint.y || currentPoint.x > highestPoint.x) {
        highestPoint = currentPoint
        println("Current highest point: $highestPoint")
    }

    if (currentPoint == Point(
            cityDefinition[0].length - 1,
            cityDefinition.size - 1
        )
    ) {
        path.fold(0L) { acc, cityMove ->
            val currentLowestForPoint = minimalHeatLossToEndForPoint.getOrPut(
                Pair(
                    cityMove.targetPoint,
                    cityMove.direction
                )
            ) { currentHeatLossLevel - acc - cityMove.heatLoss }
            if (currentLowestForPoint > currentHeatLossLevel - acc - cityMove.heatLoss) {
                minimalHeatLossToEndForPoint[Pair(cityMove.targetPoint, cityMove.direction)] =
                    currentHeatLossLevel - acc - cityMove.heatLoss
            }
            acc + cityMove.heatLoss
        }
        if(currentHeatLossLevel < currentMinimalHeatLoss){
            println("Found new minimal heat loss: $currentHeatLossLevel")

            currentMinimalHeatLoss = currentHeatLossLevel
            shortestPath = path
            return currentMinimalHeatLoss
        }
    }

    val possibleMoves = mutableListOf<CityMove>()
    if (currentPoint.x > 0) {
        if (currentHeatLossLevel + minimalHeatLossToEndForPoint.getOrDefault(
                Pair(
                    Point(
                        currentPoint.x - 1,
                        currentPoint.y
                    ), HitDirection.WEST
                ), 0
            ) < currentMinimalHeatLoss
        ) {
            possibleMoves.add(
                CityMove(
                    Point(currentPoint.x - 1, currentPoint.y),
                    HitDirection.WEST,
                    cityDefinition[currentPoint.y][currentPoint.x - 1].toString().toLong()
                )
            )
        }
    }
    if (currentPoint.x < cityDefinition[0].length - 1) {
        if (currentHeatLossLevel + minimalHeatLossToEndForPoint.getOrDefault(
                Pair(
                    Point(
                        currentPoint.x + 1,
                        currentPoint.y
                    ), HitDirection.EAST
                ), 0
            ) < currentMinimalHeatLoss
        ) {
            possibleMoves.add(
                CityMove(
                    Point(currentPoint.x + 1, currentPoint.y),
                    HitDirection.EAST,
                    cityDefinition[currentPoint.y][currentPoint.x + 1].toString().toLong()
                )
            )
        }
    }
    if (currentPoint.y > 0) {
        if (currentHeatLossLevel + minimalHeatLossToEndForPoint.getOrDefault(
                Pair(
                    Point(
                        currentPoint.x,
                        currentPoint.y - 1
                    ), HitDirection.NORTH
                ), 0
            ) < currentMinimalHeatLoss
        ) {
            possibleMoves.add(
                CityMove(
                    Point(currentPoint.x, currentPoint.y - 1),
                    HitDirection.NORTH,
                    cityDefinition[currentPoint.y - 1][currentPoint.x].toString().toLong()
                )
            )
        }
    }
    if (currentPoint.y < cityDefinition.size - 1) {
        if (currentHeatLossLevel + minimalHeatLossToEndForPoint.getOrDefault(
                Pair(
                    Point(
                        currentPoint.x,
                        currentPoint.y + 1
                    ), HitDirection.SOUTH
                ), 0
            ) < currentMinimalHeatLoss
        ) {
            possibleMoves.add(
                CityMove(
                    Point(currentPoint.x, currentPoint.y + 1),
                    HitDirection.SOUTH,
                    cityDefinition[currentPoint.y + 1][currentPoint.x].toString().toLong()
                )
            )
        }
    }
    val lastThreeMovesSameDirection = path.size > 3 && path.takeLast(3).all { it.direction == path.last().direction }
    var nextMoves =
        possibleMoves.filter { it !in path && (!lastThreeMovesSameDirection || it.direction != path.lastOrNull()?.direction) }
            .sortedBy {
                it.heatLoss
            }

    if(currentMinimalHeatLoss == Long.MAX_VALUE) {
        nextMoves = nextMoves.filter { it.direction == HitDirection.EAST } + nextMoves.filter { it.direction == HitDirection.SOUTH } + nextMoves.filter { it.direction !in listOf(HitDirection.EAST, HitDirection.SOUTH) }
    }


    if (nextMoves.isEmpty()) {
        return Long.MAX_VALUE
    }

    return nextMoves.minOf {
        calculateHeatLossByStartPoint(
            cityDefinition,
            it.targetPoint,
            path.toMutableList().apply { add(it) }
        )
    }
}

class CityMove(val targetPoint: Point, val direction: HitDirection, val heatLoss: Long) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CityMove

        return targetPoint == other.targetPoint
    }

    override fun hashCode(): Int {
        return targetPoint.hashCode()
    }

    override fun toString(): String {
        return "CityMove(targetPoint=$targetPoint, direction=$direction, heatLoss=$heatLoss)"
    }

}

enum class HitDirection {
    NORTH,
    EAST,
    SOUTH,
    WEST
}