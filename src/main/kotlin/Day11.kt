import kotlin.system.measureTimeMillis

fun main() {
    solveDay11Part(
        "Part01: Calculate shortest path between all galaxies",
        "example_input",
        "example_result",
        "input",
        calculateShortestPathBetweenAllGalaxies
    )
    solveDay11Part(
        "Part01: Calculate shortest path between all galaxies in super mega galaxy",
        "example_input2",
        "example_result2",
        "input",
        calculateShortestPathBetweenAllSuperExpandedGalaxies
    )
}

fun solveDay11Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 11)
    val exampleResult = readInput(exampleResultFileName, 11)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 11)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val calculateShortestPathBetweenAllGalaxies: (List<String>) -> Long = { galaxyMap ->
    val columnsIndicesToExpand = getColumnIndicesToExpand(galaxyMap)
    val expandedGalaxy = createExpandedGalaxy(galaxyMap, columnsIndicesToExpand, 1)
    val galaxies = findGalaxiesInGalaxyMap(expandedGalaxy)

    var shortestPathBetweenAllGalaxies = 0
    for(galaxyOuterIndex in galaxies.indices) {
        for(galaxyInnerIndex in galaxyOuterIndex +1 until galaxies.size) {
            shortestPathBetweenAllGalaxies += findShortestPathBetweenTwoGalaxies(galaxies[galaxyOuterIndex], galaxies[galaxyInnerIndex])
        }
    }
    shortestPathBetweenAllGalaxies.toLong()
}

val calculateShortestPathBetweenAllSuperExpandedGalaxies: (List<String>) -> Long = { galaxyMap ->
    val columnsIndicesToExpand = getColumnIndicesToExpand(galaxyMap)
    val rowIndicesToExpand = getRowIndicesToExpand(galaxyMap)

//    We can't do that - this will not work XD
//    val expandedGalaxy = createExpandedGalaxy(galaxyMap, columnsIndicesToExpand, 999999)

    val galaxies = findGalaxiesInGalaxyMap(galaxyMap)
    val expandedGalaxiesPositions = calculateGalaxyPositionsWithExpansionFactor(galaxies, columnsIndicesToExpand, rowIndicesToExpand, 1000000)

    var shortestPathBetweenAllGalaxies = 0L
    for(galaxyOuterIndex in expandedGalaxiesPositions.indices) {
        for(galaxyInnerIndex in galaxyOuterIndex +1 until expandedGalaxiesPositions.size) {
            shortestPathBetweenAllGalaxies += findShortestPathBetweenTwoGalaxies(expandedGalaxiesPositions[galaxyOuterIndex], expandedGalaxiesPositions[galaxyInnerIndex])
        }
    }
    shortestPathBetweenAllGalaxies
}

fun calculateGalaxyPositionsWithExpansionFactor(galaxies: List<Point>, columnsIndicesToExpand: List<Int>, rowIndicesToExpand: List<Int>, expansionFactor: Int): List<Point> {
    return galaxies.map { galaxyPosition ->
        val expandedIndicesColumn = columnsIndicesToExpand.filter { it < galaxyPosition.x }
        val expandedIndicesRow = rowIndicesToExpand.filter { it < galaxyPosition.y }

        Point(
            galaxyPosition.x - expandedIndicesColumn.size + (expandedIndicesColumn.size * expansionFactor),
            galaxyPosition.y - expandedIndicesRow.size + (expandedIndicesRow.size * expansionFactor)
        )
    }
}

fun getRowIndicesToExpand(galaxyMap: List<String>): List<Int> {
    val galaxyRowLength = galaxyMap.first().length - 1
    val galaxyColumnLength = galaxyMap.size - 1
    return (0..galaxyRowLength).mapNotNull { rowIndex ->
        var isGalaxyRow = false
        for (columnIndex in 0..galaxyColumnLength) {
            if (galaxyMap[rowIndex][columnIndex] != '.') {
                isGalaxyRow = true
                break
            }
        }
        if (!isGalaxyRow) {
            rowIndex
        } else {
            null
        }
    }
}


fun findShortestPathBetweenTwoGalaxies(galaxy1: Point, galaxy2: Point): Int {
    val horizontalSteps = if (galaxy1.x > galaxy2.x) {
        galaxy1.x - galaxy2.x
    } else {
        galaxy2.x - galaxy1.x
    }
    val verticalSteps = if (galaxy1.y > galaxy2.y) {
        galaxy1.y - galaxy2.y
    } else {
        galaxy2.y - galaxy1.y
    }
    return horizontalSteps + verticalSteps
}

fun getColumnIndicesToExpand(galaxyMap: List<String>): List<Int> {
    val galaxyRowLength = galaxyMap.first().length - 1
    val galaxyColumnLength = galaxyMap.size - 1
    return (0..galaxyColumnLength).mapNotNull { columnIndex ->
        var isGalaxyColumn = false
        for (rowIndex in 0..galaxyRowLength) {
            if (galaxyMap[rowIndex][columnIndex] != '.') {
                isGalaxyColumn = true
                break
            }
        }
        if (!isGalaxyColumn) {
            columnIndex
        } else {
            null
        }
    }
}

fun createExpandedGalaxy(galaxyMap: List<String>, columnsIndicesToExpand: List<Int>, expansionFactor: Int): List<String> {
    val expandedGalaxy = mutableListOf<String>()
    for ((y, line) in galaxyMap.withIndex()) {
        val galaxyLineBuilder = StringBuilder()
        for ((x, character) in line.withIndex()) {
            if (character != '.') {
                galaxyLineBuilder.append('#')
            } else if (columnsIndicesToExpand.contains(x)) {
                galaxyLineBuilder.append('.')
                galaxyLineBuilder.append(".".repeat(expansionFactor))
            } else {
                galaxyLineBuilder.append('.')
            }
        }
        val galaxyLineString = galaxyLineBuilder.toString()
        expandedGalaxy.add(galaxyLineString)
        if (galaxyLineString.replace(".", "").isEmpty()) {
            (0..<expansionFactor).forEach { _ -> expandedGalaxy.add(galaxyLineString) }
        }
    }
    return expandedGalaxy
}

fun findGalaxiesInGalaxyMap(expandedGalaxy: List<String>): List<Point> {
    val galaxies = mutableListOf<Point>()
    for ((y, line) in expandedGalaxy.withIndex()) {
        for ((x, character) in line.withIndex()) {
            if (character != '.') {
                galaxies.add(Point(x, y))
            }
        }
    }
    return galaxies
}