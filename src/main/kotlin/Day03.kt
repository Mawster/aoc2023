fun main() {
    solveDay03Part(
        "Part01: Find relevant part numbers",
        "example_input",
        "example_result",
        "input",
        extractRelevantPartsFromTwoDimensionalArray
    )
    solveDay03Part(
        "Part02: Calculate gear ratios",
        "example_input2",
        "example_result2",
        "input",
        calculateGearRatioFromTwoDimensionalArray
    )
}

fun solveDay03Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Int
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 3)
    val exampleResult = readInput(exampleResultFileName, 3)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    check(calculatedExampleResult == exampleResult.first().toInt())
    println("Calculated example is right! Result: $calculatedExampleResult")

    println("Calculating result for given input file $challengeInputFileName with solver function")
    val challengeInput = readInput(challengeInputFileName, 3)
    val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
    println("Calculated challenge result: $challengeResult")
}

val extractRelevantPartsFromTwoDimensionalArray: (List<String>) -> Int = { twoDimensionalArray ->
    val partsWithAdjacentSymbols = getPartsWithAdjacentSymbols(twoDimensionalArray)
    partsWithAdjacentSymbols.sumOf { it.partNumber }
}

val calculateGearRatioFromTwoDimensionalArray: (List<String>) -> Int = { twoDimensionalArray ->
    val partsWithAdjacentSymbols = getPartsWithAdjacentSymbols(twoDimensionalArray)
    val partsAtGear = mutableMapOf<Point, MutableList<Int>>()
    partsWithAdjacentSymbols.forEach { part ->
        part.getGearSymbols().forEach { gearSymbol ->
            partsAtGear.getOrPut(gearSymbol.pointInScheme) { mutableListOf() }.add(part.partNumber)
        }
    }
    partsAtGear.filterValues { it.size > 1 }.entries.sumOf { it.value.reduce { acc, partNumber -> (acc * partNumber) } }
}

fun getPartsWithAdjacentSymbols(twoDimensionalArray: List<String>): List<Part> {
    val partRegex = Regex("(\\d+)")
    val partsWithAdjacentSymbols = mutableListOf<Part>()
    twoDimensionalArray.forEachIndexed { lineIndex, line ->
        val foundPartNumbers = partRegex.findAll(line)
        for (foundPartNumber in foundPartNumbers) {
            val pointsToCheckForSymbol = getPointsToCheckInScheme(foundPartNumber.range, lineIndex)
            val symbolsForPart = pointsToCheckForSymbol
                .filter { doesTwoDimensionalArrayContainSymbolAtPoint(twoDimensionalArray, it) }
                .map { Symbol(twoDimensionalArray[it.x][it.y], it) }
            if (symbolsForPart.isNotEmpty()) {
                partsWithAdjacentSymbols.add(Part(foundPartNumber.value.toInt(), symbolsForPart))
            }
        }
    }
    return partsWithAdjacentSymbols
}

fun getPointsToCheckInScheme(indicesOfPart: IntRange, currentLineInScheme: Int): List<Point> {
    val partFirstIndex = indicesOfPart.first
    val partLastIndex = indicesOfPart.last
    val pointsToCheck = mutableListOf<Point>()
    indicesOfPart.forEach { index ->
        pointsToCheck.add(Point(currentLineInScheme - 1, index))
        pointsToCheck.add(Point(currentLineInScheme + 1, index))
        if (index == partFirstIndex) {
            pointsToCheck.add(Point(currentLineInScheme, index - 1))
            pointsToCheck.add(Point(currentLineInScheme - 1, index - 1))
            pointsToCheck.add(Point(currentLineInScheme + 1, index - 1))
        }
        if (index == partLastIndex) {
            pointsToCheck.add(Point(currentLineInScheme, index + 1))
            pointsToCheck.add(Point(currentLineInScheme - 1, index + 1))
            pointsToCheck.add(Point(currentLineInScheme + 1, index + 1))
        }
    }
    return pointsToCheck
}

fun isValidCheckInScheme(
    twoDimensionalArray: List<String>,
    rowIndex: Int,
    columnIndex: Int
): Boolean {
    val columnMaxIndex = twoDimensionalArray[0].length - 1
    val rowMaxIndex = twoDimensionalArray.size - 1
    return !(columnIndex > columnMaxIndex || columnIndex < 0 || rowIndex > rowMaxIndex || rowIndex < 0)
}

fun doesTwoDimensionalArrayContainSymbolAtPoint(
    twoDimensionalArray: List<String>,
    point: Point
): Boolean {
    if (isValidCheckInScheme(
            twoDimensionalArray,
            point.x,
            point.y
        )
    ) {
        val charAtCoordinates = twoDimensionalArray[point.x][point.y]
        return !(charAtCoordinates.isDigit() || charAtCoordinates == '.')
    }
    return false
}

data class Point(val x: Int, val y: Int)

data class Part(val partNumber: Int, val symbols: List<Symbol>) {
    fun getGearSymbols(): List<Symbol> {
        return symbols.filter { it.value == '*' }
    }
}

data class Symbol(val value: Char, val pointInScheme: Point)