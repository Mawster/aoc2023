import kotlin.system.measureTimeMillis

fun main() {
    solveDay14Part(
        "Part01: Calculate sum of the load on the reflector dish",
        "example_input",
        "example_result",
        "input",
        calculateLoadOfReflectorDish
    )
    solveDay14Part(
        "Part01: Calculate sum of the load on the reflector dish after 1000000000 cycles",
        "example_input2",
        "example_result2",
        "input",
        calculateLoadOfReflectorDishWithCycling
    )
}

fun solveDay14Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 14)
    val exampleResult = readInput(exampleResultFileName, 14)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        println("Calculated example Result: $calculatedExampleResult")
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 14)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val calculateLoadOfReflectorDish: (List<String>) -> Long = { reflectorDishLayoutInput: List<String> ->
    val reflectorDishLayout = ReflectorDishLayout(reflectorDishLayoutInput)
    reflectorDishLayout.tilt(TiltDirection.NORTH)

    reflectorDishLayout.rockLocations.sumOf { reflectorDishLayout.layoutRowCount - it.y }.toLong()
}

val calculateLoadOfReflectorDishWithCycling: (List<String>) -> Long = { reflectorDishLayoutInput: List<String> ->
    val reflectorDishLayout = ReflectorDishLayout(reflectorDishLayoutInput)
    val resultInCycles = mutableMapOf<Long, MutableList<Long>>()

    for (cycle in 0 until 1000000000) {
        reflectorDishLayout.tilt(TiltDirection.NORTH)
        reflectorDishLayout.tilt(TiltDirection.WEST)
        reflectorDishLayout.tilt(TiltDirection.SOUTH)
        reflectorDishLayout.tilt(TiltDirection.EAST)
        resultInCycles.getOrPut(
            reflectorDishLayout.rockLocations.sumOf { reflectorDishLayout.layoutRowCount - it.y }.toLong()
        ) { mutableListOf() }.add(cycle.toLong())
        if (cycle % 1000000 == 0) {
            println("Cycle $cycle")
        }
    }

    reflectorDishLayout.rockLocations.sumOf { reflectorDishLayout.layoutRowCount - it.y }.toLong()
}

class ReflectorDishLayout(reflectorDishLayout: List<String>) {
    var rockLocations = mutableListOf<Point>()
    var stopperLocations = mutableListOf<Point>()
    val layoutRowCount: Int
    val layoutColumnCount: Int

    init {
        reflectorDishLayout.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, char ->
                when (char) {
                    'O' -> rockLocations.add(Point(columnIndex, rowIndex))
                    '#' -> stopperLocations.add(Point(columnIndex, rowIndex))
                }
            }
        }
        layoutRowCount = reflectorDishLayout.size
        layoutColumnCount = reflectorDishLayout[0].length
    }

    fun tilt(tiltDirection: TiltDirection) {
        when (tiltDirection) {
            TiltDirection.NORTH -> tiltNorth()
            TiltDirection.WEST -> tiltWest()
            TiltDirection.SOUTH -> tiltSouth()
            TiltDirection.EAST -> tiltEast()
        }
    }

    private fun tiltNorth() {
        val newRockLocations = mutableListOf<Point>()
        for (column in 0 until this.layoutColumnCount) {
            var targetRowIndex = 0
            val stoppers = this.stopperLocations.filter { it.x == column }
            stoppers.map { stopper ->
                val rocksBetweenStopperAndTargetIndex =
                    this.rockLocations.filter { it.x == stopper.x && it.y >= targetRowIndex && it.y < stopper.y }
                rocksBetweenStopperAndTargetIndex.forEachIndexed { index, point ->
                    newRockLocations.add(Point(point.x, targetRowIndex + index))
                }
                targetRowIndex = stopper.y + 1
            }
            val rocksBetweenStopperAndTargetIndex =
                this.rockLocations.filter { it.x == column && it.y >= targetRowIndex && it.y < this.layoutRowCount }
            rocksBetweenStopperAndTargetIndex.forEachIndexed { index, point ->
                newRockLocations.add(Point(point.x, targetRowIndex + index))
            }
        }
        this.rockLocations = newRockLocations
    }

    private fun tiltSouth() {
        val newRockLocations = mutableListOf<Point>()
        for (column in 0 until this.layoutColumnCount) {
            var targetRowIndex = this.layoutRowCount - 1
            val stoppers = this.stopperLocations.filter { it.x == column }
            stoppers.sortedByDescending { it.y }.map { stopper ->
                val rocksBetweenStopperAndTargetIndex =
                    this.rockLocations.filter { it.x == stopper.x && it.y <= targetRowIndex && it.y > stopper.y }
                rocksBetweenStopperAndTargetIndex.forEachIndexed { index, point ->
                    newRockLocations.add(Point(point.x, targetRowIndex - index))
                }
                targetRowIndex = stopper.y - 1
            }
            val rocksBetweenStopperAndTargetIndex =
                this.rockLocations.filter { it.x == column && it.y <= targetRowIndex && it.y >= 0 }
            rocksBetweenStopperAndTargetIndex.forEachIndexed { index, point ->
                newRockLocations.add(Point(point.x, targetRowIndex - index))
            }
        }
        this.rockLocations = newRockLocations
    }

    private fun tiltWest() {
        val newRockLocations = mutableListOf<Point>()
        for (row in 0 until this.layoutRowCount) {
            var targetRowIndex = 0
            val stoppers = this.stopperLocations.filter { it.y == row }
            stoppers.map { stopper ->
                val rocksBetweenStopperAndTargetIndex =
                    this.rockLocations.filter { it.y == stopper.y && it.x >= targetRowIndex && it.x < stopper.x }
                rocksBetweenStopperAndTargetIndex.forEachIndexed { index, point ->
                    newRockLocations.add(Point(targetRowIndex + index, point.y))
                }
                targetRowIndex = stopper.x + 1
            }
            val rocksBetweenStopperAndTargetIndex =
                this.rockLocations.filter { it.y == row && it.x >= targetRowIndex && it.x < this.layoutColumnCount }
            rocksBetweenStopperAndTargetIndex.forEachIndexed { index, point ->
                newRockLocations.add(Point(targetRowIndex + index, point.y))
            }
        }
        this.rockLocations = newRockLocations
    }

    private fun tiltEast() {
        val newRockLocations = mutableListOf<Point>()
        for (row in 0 until this.layoutRowCount) {
            var targetRowIndex = this.layoutColumnCount - 1
            val stoppers = this.stopperLocations.filter { it.y == row }
            stoppers.sortedByDescending { it.x }.map { stopper ->
                val rocksBetweenStopperAndTargetIndex =
                    this.rockLocations.filter { it.y == stopper.y && it.x <= targetRowIndex && it.x > stopper.x }
                rocksBetweenStopperAndTargetIndex.forEachIndexed { index, point ->
                    newRockLocations.add(Point(targetRowIndex - index, point.y))
                }
                targetRowIndex = stopper.x - 1
            }
            val rocksBetweenStopperAndTargetIndex =
                this.rockLocations.filter { it.y == row && it.x <= targetRowIndex && it.x >= 0 }
            rocksBetweenStopperAndTargetIndex.forEachIndexed { index, point ->
                newRockLocations.add(Point(targetRowIndex - index, point.y))
            }
        }
        this.rockLocations = newRockLocations
    }

    fun hasSameRockLocations(rockLocations: List<Point>): Boolean {
        return this.rockLocations.size == rockLocations.size && this.rockLocations.containsAll(rockLocations)
    }

    override fun toString(): String {
        val reflectorDishLayoutStringBuilder = StringBuilder()
        for (rowIndex in 0 until this.layoutRowCount) {
            for (columnIndex in 0 until this.layoutColumnCount) {
                val point = Point(columnIndex, rowIndex)
                when {
                    this.rockLocations.contains(point) -> reflectorDishLayoutStringBuilder.append('O')
                    this.stopperLocations.contains(point) -> reflectorDishLayoutStringBuilder.append('#')
                    else -> reflectorDishLayoutStringBuilder.append('.')
                }
            }
            reflectorDishLayoutStringBuilder.append('\n')
        }
        return reflectorDishLayoutStringBuilder.toString()
    }
}

enum class TiltDirection {
    NORTH, WEST, SOUTH, EAST
}