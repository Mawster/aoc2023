import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

fun main() {
    /*    solveDay18Part(
            "Part01: Calculate trench size",
            "example_input",
            "example_result",
            "input",
            calculateTrenchSize
        )*/
    solveDay18Part(
        "Part01: Calculate big trench size",
        "example_input2",
        "example_result2",
        "input",
        calculateBigTrenchSize
    )
}

fun solveDay18Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 18)
    val exampleResult = readInput(exampleResultFileName, 18)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        println("Calculated example Result: $calculatedExampleResult")
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 18)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val calculateTrenchSize: (List<String>) -> Long = { digPlanInput: List<String> ->
    val digLines = mutableListOf<DigLine>()
    digPlanInput.forEachIndexed { index, it ->
        val digPlanRowParts = it.split(" ")
        val direction = digPlanRowParts[0]
        val digLength = if (index == 0) {
            digPlanRowParts[1].toInt() + 1
        } else {
            digPlanRowParts[1].toInt()
        }
        val startPoint = if (digLines.isEmpty()) {
            Point(0, 0)
        } else {
            getNewPointAfterDigging(digLines.last().endPoint, direction, 1)
        }
        digLines.add(
            DigLine(
                startPoint,
                getNewPointAfterDigging(startPoint, direction, digLength - 1),
                digLength,
                digPlanRowParts[2].replace("(", "").replace(")", "")
            )
        )
    }

    val shiftYAxis = digLines.minByOrNull { it.startPoint.y }!!.startPoint.y.absoluteValue
    val pointsOfDig = digLines.map { it.shiftYAxisDown(shiftYAxis) }.map { it.getPointsOfLine() }.flatten()
    val digSiteMaxColumnsAndRowsIndex = Pair(
        pointsOfDig.maxByOrNull { it.x }!!.x,
        pointsOfDig.maxByOrNull { it.y }!!.y
    )
    val maxPossibleArea = (digSiteMaxColumnsAndRowsIndex.first + 1) * (digSiteMaxColumnsAndRowsIndex.second + 1)

    val possibleFloodFillStartPoints = ((0..digSiteMaxColumnsAndRowsIndex.first).map { Point(it, 0) } +
            (0..digSiteMaxColumnsAndRowsIndex.second).map { Point(0, it) } +
            (0..digSiteMaxColumnsAndRowsIndex.first).map { Point(it, digSiteMaxColumnsAndRowsIndex.second) } +
            (0..digSiteMaxColumnsAndRowsIndex.second).map { Point(digSiteMaxColumnsAndRowsIndex.first, it) }).toSet()
        .filter { !pointsOfDig.contains(it) }
    val digSite =
        Array(digSiteMaxColumnsAndRowsIndex.second + 1) { Array(digSiteMaxColumnsAndRowsIndex.first + 1) { false } }
    pointsOfDig.forEach {
        digSite[it.y][it.x] = true
    }

    maxPossibleArea - calculateFloodFillAreaFromPoints(possibleFloodFillStartPoints, digSite)
}

val calculateBigTrenchSize: (List<String>) -> Long = { digPlanInput: List<String> ->
    val digLines = mutableListOf<LongDigLine>()
    val digPoints = mutableListOf<LongPoint>()
    digPlanInput.forEachIndexed { index, it ->
        val digPlanRowParts = it.split(" ")
        val hexadecimalString = digPlanRowParts[2].replace("(#", "").replace(")", "")

        val direction = hexadecimalString.last().getDirection()

        val digLength = if (index == 0) {
            hexadecimalString.take(5).toInt(16) + 1
        } else {
            hexadecimalString.take(5).toInt(16)
        }
        val startPoint = if (digLines.isEmpty()) {
            LongPoint(1L, 1L)
        } else {
            getLongNewPointAfterDigging(digLines.last().endPoint, direction, 1)
        }
        digLines.add(
            LongDigLine(
                startPoint,
                getLongNewPointAfterDigging(startPoint, direction, digLength - 1),
                digLength,
                digPlanRowParts[2].replace("#(", "").replace(")", ""),
                index
            )
        )
    }

    val digLinesSorted = digLines.sortedBy { it.id }

    digPoints.add(digLinesSorted.first().startPoint)
    digPoints.add(digLinesSorted.first().endPoint)
    digLinesSorted.drop(1).dropLast(1).forEach { digPoints.add(it.endPoint) }

    val sortedForShoelace = sortPointsClockwise(digPoints)

    calculatePolygonArea(sortedForShoelace)
}

fun calculatePolygonArea(points: List<LongPoint>): Long {
    var sum1 = 0L
    var sum2 = 0L
    val size = points.size

    for (i in 0 until size) {
        val j = (i + 1) % size
        sum1 += points[i].x * points[j].y
        sum2 += points[j].x * points[i].y
    }

    return Math.abs(sum1 - sum2) / 2
}

fun calculateCentroid(points: List<LongPoint>): LongPoint {
    val centroidX = points.sumOf { it.x } / points.size
    val centroidY = points.sumOf { it.y } / points.size
    return LongPoint(centroidX, centroidY)
}

fun sortPointsClockwise(points: List<LongPoint>): List<LongPoint> {
    val centroid = calculateCentroid(points)
    return points.sortedWith(compareBy { atan2((it.y - centroid.y).toDouble(), (it.x - centroid.x).toDouble()) })
}

data class LongPoint(val x: Long, val y: Long)

fun calculateFloodFillAreaFromPoints(points: List<Point>, digSite: Array<Array<Boolean>>): Long {
    val floodFillQueue = points.toMutableList()
    var floodFillArea = 0L
    while (floodFillQueue.isNotEmpty()) {
        val currentPoint = floodFillQueue.removeAt(0)
        if (currentPoint.x < 0 || currentPoint.x > digSite[0].size - 1 || currentPoint.y < 0 || currentPoint.y > digSite.size - 1) {
            continue
        }
        if (digSite[currentPoint.y][currentPoint.x]) {
            continue
        }
        digSite[currentPoint.y][currentPoint.x] = true
        floodFillArea++
        floodFillQueue.add(Point(currentPoint.x + 1, currentPoint.y))
        floodFillQueue.add(Point(currentPoint.x - 1, currentPoint.y))
        floodFillQueue.add(Point(currentPoint.x, currentPoint.y + 1))
        floodFillQueue.add(Point(currentPoint.x, currentPoint.y - 1))
    }
    return floodFillArea
}

fun getNewPointAfterDigging(point: Point, direction: String, length: Int): Point {
    return when (direction) {
        "R" -> Point(point.x + length, point.y)
        "D" -> Point(point.x, point.y + length)
        "L" -> Point(point.x - length, point.y)
        "U" -> Point(point.x, point.y - length)
        else -> throw IllegalArgumentException("Unknown direction $direction")
    }
}

fun getLongNewPointAfterDigging(point: LongPoint, direction: String, length: Int): LongPoint {
    return when (direction) {
        "R" -> LongPoint(point.x + length, point.y)
        "D" -> LongPoint(point.x, point.y + length)
        "L" -> LongPoint(point.x - length, point.y)
        "U" -> LongPoint(point.x, point.y - length)
        else -> throw IllegalArgumentException("Unknown direction $direction")
    }
}

data class DigLine(
    val startPoint: Point,
    val endPoint: Point,
    val length: Int,
    val colorCode: String,
    val id: Int = 0
) {
    fun getPointsOfLine(): List<Point> {
        return if (startPoint.x == endPoint.x) {
            (min(startPoint.y, endPoint.y)..max(startPoint.y, endPoint.y)).map {
                Point(startPoint.x, it)
            }
        } else {
            (min(startPoint.x, endPoint.x)..max(startPoint.x, endPoint.x)).map {
                Point(it, startPoint.y)
            }
        }
    }

    fun shiftYAxisDown(shift: Int): DigLine {
        return DigLine(
            Point(startPoint.x, startPoint.y + shift),
            Point(endPoint.x, endPoint.y + shift),
            length,
            colorCode
        )
    }
}

data class LongDigLine(
    val startPoint: LongPoint,
    val endPoint: LongPoint,
    val length: Int,
    val colorCode: String,
    val id: Int = 0
) {
    fun getPointsOfLine(): List<LongPoint> {
        return if (startPoint.x == endPoint.x) {
            (min(startPoint.y, endPoint.y)..max(startPoint.y, endPoint.y)).map {
                LongPoint(startPoint.x, it)
            }
        } else {
            (min(startPoint.x, endPoint.x)..max(startPoint.x, endPoint.x)).map {
                LongPoint(it, startPoint.y)
            }
        }
    }

    fun shiftYAxisDown(shift: Int): LongDigLine {
        return LongDigLine(
            LongPoint(startPoint.x, startPoint.y + shift),
            LongPoint(endPoint.x, endPoint.y + shift),
            length,
            colorCode
        )
    }
}

fun Char.getDirection(): String {
    return when (this) {
        '0' -> "R"
        '1' -> "D"
        '2' -> "L"
        '3' -> "U"
        else -> throw IllegalArgumentException("Unknown direction $this")
    }
}