import kotlin.system.measureTimeMillis

fun main() {
    solveDay16Part(
        "Part01: Calculate energedized tiles",
        "example_input",
        "example_result",
        "input",
        calculateEnergizedTiles
    )
    solveDay16Part(
        "Part01: Calculate max energedized tiles",
        "example_input2",
        "example_result2",
        "input",
        calculateMaxEnergizedTiles
    )
}

fun solveDay16Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 16)
    val exampleResult = readInput(exampleResultFileName, 16)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        println("Calculated example Result: $calculatedExampleResult")
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 16)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

var inputRows = 0
var inputColumns = 0
var energizedTiles = mutableSetOf<Point>()

val calculateEnergizedTiles: (List<String>) -> Long = { contraptionDefinition: List<String> ->
    inputRows = contraptionDefinition.size
    inputColumns = contraptionDefinition.first().length
    energizedTiles = mutableSetOf()
    val beamManipulators = contraptionDefinition.mapIndexed { rowIndex, line ->
        line.mapIndexedNotNull { columnIndex, char ->
            when (char) {
                '.' -> null
                else -> BeamManipulator(Point(columnIndex, rowIndex), char)
            }
        }
    }.flatten()

    val beamStartingPoint = Point(0, 0)
    beamManipulators.first().reflectBeamFromPointWithinField(beamStartingPoint, beamManipulators, HitDirection.WEST)
    energizedTiles.size.toLong()
}

val calculateMaxEnergizedTiles: (List<String>) -> Long = { contraptionDefinition: List<String> ->
    inputRows = contraptionDefinition.size
    inputColumns = contraptionDefinition.first().length
    val beamManipulators = contraptionDefinition.mapIndexed { rowIndex, line ->
        line.mapIndexedNotNull { columnIndex, char ->
            when (char) {
                '.' -> null
                else -> BeamManipulator(Point(columnIndex, rowIndex), char)
            }
        }
    }.flatten()


    val maxEnergizedValues = mutableSetOf<Long>()
    for(row in 0 until inputRows){
        energizedTiles = mutableSetOf()
        var beamStartingPoint = Point(0, row)
        beamManipulators.filter { it.location.y == row }.minByOrNull { it.location.x }?.reflectBeamFromPointWithinField(beamStartingPoint, beamManipulators, HitDirection.WEST)
        maxEnergizedValues.add(energizedTiles.size.toLong())
        beamManipulators.forEach { it.reset() }

        energizedTiles = mutableSetOf()
        beamStartingPoint = Point(inputColumns - 1, row)
        beamManipulators.filter { it.location.y == row }.maxByOrNull { it.location.x }?.reflectBeamFromPointWithinField(beamStartingPoint, beamManipulators, HitDirection.EAST)
        maxEnergizedValues.add(energizedTiles.size.toLong())
        beamManipulators.forEach { it.reset() }
    }

    for(column in 0 until inputColumns){
        energizedTiles = mutableSetOf()
        var beamStartingPoint = Point(column, 0)
        beamManipulators.filter { it.location.x == column }.minByOrNull { it.location.y }?.reflectBeamFromPointWithinField(beamStartingPoint, beamManipulators, HitDirection.NORTH)
        maxEnergizedValues.add(energizedTiles.size.toLong())
        beamManipulators.forEach { it.reset() }

        energizedTiles = mutableSetOf()
        beamStartingPoint = Point(column, inputRows - 1)
        beamManipulators.filter { it.location.x == column }.maxByOrNull { it.location.y }?.reflectBeamFromPointWithinField(beamStartingPoint, beamManipulators, HitDirection.SOUTH)
        maxEnergizedValues.add(energizedTiles.size.toLong())
        beamManipulators.forEach { it.reset() }
    }

    maxEnergizedValues.max()
}

data class BeamManipulator(val location: Point, val type: Char) {
    private val hitFromDirections = mutableSetOf<HitDirection>()

    fun reflectBeamFromPointWithinField(beamStartingPoint: Point, beamManipulators: List<BeamManipulator>, beamDirectionStart: HitDirection? = null): Long {
        val hitDirection = beamDirectionStart ?: determineHitDirection(beamStartingPoint)
        if (hitFromDirections.contains(hitDirection)) {
            return 0L
        }
        hitFromDirections.add(hitDirection)

        val energizedTiles = determineEnergizedTilesCount(beamStartingPoint, hitDirection)
        return energizedTiles + determineNextBeamManipulators(beamManipulators, hitDirection).sumOf { beamManipulator ->
            beamManipulator.reflectBeamFromPointWithinField(this.location, beamManipulators)
        }
    }

    private fun determineNextBeamManipulators(
        beamManipulators: List<BeamManipulator>,
        hitDirection: HitDirection
    ): List<BeamManipulator> {
        return when (type) {
            '|' -> {
                when (hitDirection) {
                    HitDirection.NORTH -> findNextManipulatorToDirection(beamManipulators, HitDirection.SOUTH)
                    HitDirection.SOUTH -> findNextManipulatorToDirection(beamManipulators, HitDirection.NORTH)
                    HitDirection.EAST, HitDirection.WEST -> listOf(
                        findNextManipulatorToDirection(beamManipulators, HitDirection.NORTH),
                        findNextManipulatorToDirection(beamManipulators, HitDirection.SOUTH)
                    ).flatten()
                }
            }

            '-' -> {
                when (hitDirection) {
                    HitDirection.NORTH, HitDirection.SOUTH -> listOf(
                        findNextManipulatorToDirection(beamManipulators, HitDirection.EAST),
                        findNextManipulatorToDirection(beamManipulators, HitDirection.WEST)
                    ).flatten()

                    HitDirection.EAST -> findNextManipulatorToDirection(beamManipulators, HitDirection.WEST)
                    HitDirection.WEST -> findNextManipulatorToDirection(beamManipulators, HitDirection.EAST)
                }
            }

            '/' -> {
                when (hitDirection) {
                    HitDirection.NORTH -> findNextManipulatorToDirection(beamManipulators, HitDirection.WEST)
                    HitDirection.EAST -> findNextManipulatorToDirection(beamManipulators, HitDirection.SOUTH)
                    HitDirection.SOUTH -> findNextManipulatorToDirection(beamManipulators, HitDirection.EAST)
                    HitDirection.WEST -> findNextManipulatorToDirection(beamManipulators, HitDirection.NORTH)
                }
            }

            '\\' -> {
                when (hitDirection) {
                    HitDirection.NORTH -> findNextManipulatorToDirection(beamManipulators, HitDirection.EAST)
                    HitDirection.EAST -> findNextManipulatorToDirection(beamManipulators, HitDirection.NORTH)
                    HitDirection.SOUTH -> findNextManipulatorToDirection(beamManipulators, HitDirection.WEST)
                    HitDirection.WEST -> findNextManipulatorToDirection(beamManipulators, HitDirection.SOUTH)
                }
            }

            '.' -> listOf()
            else -> throw IllegalStateException("Beam manipulator $this is not a valid beam manipulator")
        }
    }

    private fun findNextManipulatorToDirection(
        beamManipulators: List<BeamManipulator>,
        hitDirection: HitDirection
    ): List<BeamManipulator> {
        var nextBeamManipulators = when (hitDirection) {
            HitDirection.NORTH -> {
                beamManipulators.filter { it.location.x == this.location.x && it.location.y < this.location.y }
                    .sortedByDescending { it.location.y }.take(1)
            }

            HitDirection.SOUTH -> {
                beamManipulators.filter { it.location.x == this.location.x && it.location.y > this.location.y }
                    .sortedBy { it.location.y }.take(1)
            }

            HitDirection.EAST -> {
                beamManipulators.filter { it.location.y == this.location.y && it.location.x > this.location.x }
                    .sortedBy { it.location.x }.take(1)
            }

            HitDirection.WEST -> {
                beamManipulators.filter { it.location.y == this.location.y && it.location.x < this.location.x }
                    .sortedByDescending { it.location.x }.take(1)
            }
        }
        if (nextBeamManipulators.isEmpty()) {
            nextBeamManipulators = when (hitDirection) {
                HitDirection.NORTH -> listOf(BeamManipulator(Point(this.location.x, 0), '.'))
                HitDirection.EAST -> listOf(BeamManipulator(Point(inputColumns - 1, this.location.y), '.'))
                HitDirection.SOUTH -> listOf(BeamManipulator(Point(this.location.x, inputRows - 1), '.'))
                HitDirection.WEST -> listOf(BeamManipulator(Point(0, this.location.y), '.'))
            }
        }
        return nextBeamManipulators.filter { it.location != this.location }
    }

    private fun determineEnergizedTilesCount(beamStartingPoint: Point, hitDirection: HitDirection): Long {
        when (hitDirection) {
            HitDirection.NORTH -> {
                (beamStartingPoint.y..this.location.y).forEach { y ->
                    energizedTiles.add(Point(this.location.x, y))
                }
            }
            HitDirection.EAST -> {
                (this.location.x..beamStartingPoint.x).forEach { x ->
                    energizedTiles.add(Point(x, this.location.y))
                }
            }
            HitDirection.SOUTH -> {
                (this.location.y..beamStartingPoint.y).forEach { y ->
                    energizedTiles.add(Point(this.location.x, y))
                }
            }
            HitDirection.WEST -> {
                (beamStartingPoint.x..this.location.x).forEach { x ->
                    energizedTiles.add(Point(x, this.location.y))
                }
            }
        }
        return 0L
    }

    private fun determineHitDirection(beamStartingPoint: Point): HitDirection {
        if (beamStartingPoint.x == location.x && beamStartingPoint.y < location.y) {
            return HitDirection.NORTH
        } else if (beamStartingPoint.x == location.x && beamStartingPoint.y > location.y) {
            return HitDirection.SOUTH
        } else if (beamStartingPoint.x < location.x && beamStartingPoint.y == location.y) {
            return HitDirection.WEST
        } else if (beamStartingPoint.x > location.x && beamStartingPoint.y == location.y) {
            return HitDirection.EAST
        }
        throw IllegalStateException("Beam starting point $beamStartingPoint is not on the splitter $location")
    }

    fun reset(){
        hitFromDirections.clear()
    }
}

//enum class HitDirection {
//    NORTH,
//    EAST,
//    SOUTH,
//    WEST
//}