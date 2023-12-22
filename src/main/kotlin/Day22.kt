import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

fun main() {
    solveDay22Part(
        "Part01: Check bricks which can be disintegrated",
        "example_input",
        "example_result",
        "input",
        getCountOfDisintegratableBricks
    )

    solveDay22Part(
        "Part01: Calculate chain reaction of disintegrating bricks",
        "example_input2",
        "example_result2",
        "input",
        calculateChainReaction
    )
}

fun solveDay22Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 22)
    val exampleResult = readInput(exampleResultFileName, 22)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        println("Calculated example Result: $calculatedExampleResult")
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 22)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val fallenBricks = mutableListOf<Brick>()

val calculateChainReaction: (List<String>) -> Long = { brickDefinitionLines: List<String> ->
    val initialBrickSnapshot = getInitialBrickLocations(brickDefinitionLines)
    val brickEndLocation = letTheBricksFall(initialBrickSnapshot)

    brickEndLocation.sortedByDescending { max(it.start.z, it.end.z) }.sumOf {
        fallenBricks.clear()
        it.getChainReactionValue()
    }
}

val getCountOfDisintegratableBricks: (List<String>) -> Long = { brickDefinitionLines: List<String> ->
    val initialBrickSnapshot = getInitialBrickLocations(brickDefinitionLines)
    val brickEndLocation = letTheBricksFall(initialBrickSnapshot)

    brickEndLocation.sumOf { if (it.canBeRemoved()) 1L else 0L }
}

fun letTheBricksFall(initialBrickSnapshot: List<Brick>): List<Brick> {
    val bricks = initialBrickSnapshot.toMutableList()
    val brickEndLocation = mutableListOf<Brick>()
    while (bricks.isNotEmpty()) {
        val brick = bricks.removeAt(0)
        while (!brick.isOntheGround() && brick.supportingBricks.size == 0) {
            val bricksOneLevelLower =
                brickEndLocation.filter { max(it.start.z, it.end.z) == min(brick.start.z, brick.end.z) - 1 }
            if (bricksOneLevelLower.isEmpty()) {
                brick.letItFall()
                continue
            } else {
                bricksOneLevelLower.forEach { possibleSupportingBrick ->
                    if (possibleSupportingBrick.pointsInLevel.intersect(brick.pointsInLevel).isNotEmpty()) {
                        brick.addSupportingBrick(possibleSupportingBrick)
                        possibleSupportingBrick.addSupportedBrick(brick)
                    }
                }
                if (brick.supportingBricks.size == 0) {
                    brick.letItFall()
                }
            }
        }
        brickEndLocation.add(brick)
    }
    return brickEndLocation
}

fun getInitialBrickLocations(brickDefinitionLines: List<String>): List<Brick> {
    val bricks = mutableListOf<Brick>()
    brickDefinitionLines.forEach { brickDefinitionLine ->
        brickDefinitionLine.split("~").windowed(2) { (start, end) ->
            bricks.add(Brick(getBrickEdgeDefinition(start), getBrickEdgeDefinition(end)))
        }
    }
    return bricks.sortedBy { min(it.start.z, it.end.z) }
}

fun getBrickEdgeDefinition(definition: String): BrickEdgeDefinition {
    val (x, y, z) = definition.split(",").map { it.toInt() }
    return BrickEdgeDefinition(x, y, z)
}

class Brick(val start: BrickEdgeDefinition, val end: BrickEdgeDefinition) {
    val supportedBricks = mutableSetOf<Brick>()
    val supportingBricks = mutableSetOf<Brick>()
    val pointsInLevel: Set<Point>

    init {
        val points = mutableSetOf<Point>()
        for (x in start.x..end.x) {
            for (y in start.y..end.y) {
                points.add(Point(x, y))
            }
        }
        pointsInLevel = points
    }

    fun letItFall() {
        start.z--
        end.z--
    }

    fun isOntheGround(): Boolean {
        return start.z == 1 || end.z == 1
    }

    fun getChainReactionValue(): Long {
        fallenBricks.add(this)
        if (supportedBricks.isEmpty()) {
            return 0L
        }
        val bricksUpTheChainWhoWouldFall = supportedBricks.filter { fallenBricks.containsAll(it.supportingBricks) }
        val chainReactionValue = bricksUpTheChainWhoWouldFall.size + bricksUpTheChainWhoWouldFall.sumOf { it.getChainReactionValue() }

        return chainReactionValue
    }

    fun canBeRemoved(): Boolean {
        return supportedBricks.all { it.supportingBricks.size > 1 }
    }

    fun addSupportedBrick(brick: Brick) {
        supportedBricks.add(brick)
    }

    fun addSupportingBrick(brick: Brick) {
        supportingBricks.add(brick)
    }
}

data class BrickEdgeDefinition(var x: Int, var y: Int, var z: Int)



