import kotlin.system.measureTimeMillis

fun main() {
    solveDay08Part(
        "Part01: Calculate steps to reach ZZZ",
        "example_input",
        "example_result",
        "input",
        calculateStepsToReachEnd
    )
    solveDay08Part(
        "Part02: Calculate steps to reach XXZ on each path",
        "example_input2",
        "example_result2",
        "input",
        calculateStepsToReachEndOnAllPaths
    )
}

fun solveDay08Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 8)
    val exampleResult = readInput(exampleResultFileName, 8)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 8)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

fun parseMovementStepsAndPathsFromInputLines(inputLines: List<String>): Pair<List<Char>, MutableMap<String, Pair<String, String>>> {
    val movementSteps = inputLines[0].map { it }
    val pathExtractorRegex = Regex("(\\w{3})")
    val paths = mutableMapOf<String, Pair<String, String>>()
    inputLines.takeLast(inputLines.size - 2)
        .map { pathExtractorRegex.findAll(it).toList() }
        .associateByTo(paths, { it[0].value }, { Pair(it[1].value, it[2].value) })
    return Pair(movementSteps, paths)
}

fun makeStep(currentLocation: String, c: Char, paths: MutableMap<String, Pair<String, String>>): String {
    val currentPath = paths[currentLocation]!!
    return when (c) {
        'L' -> currentPath.first
        'R' -> currentPath.second
        else -> throw Exception("Unknown movement step $c")
    }
}

val calculateStepsToReachEnd: (List<String>) -> Long = { inputLines ->
    val movementStepsAndPaths = parseMovementStepsAndPathsFromInputLines(inputLines)
    calculateRequiredStepsWithMovementsStepsAndGivenPaths(movementStepsAndPaths.first, movementStepsAndPaths.second)
}

fun calculateRequiredStepsWithMovementsStepsAndGivenPaths(
    movementSteps: List<Char>,
    paths: MutableMap<String, Pair<String, String>>
): Long {
    var currentLocation = "AAA"
    var neededSteps = 0L
    val countSteps = movementSteps.size
    var nextStepIndex = 0

    while (currentLocation != "ZZZ") {
        if (nextStepIndex == countSteps) {
            nextStepIndex = 0
        }
        currentLocation = makeStep(currentLocation, movementSteps[nextStepIndex], paths)
        nextStepIndex++
        neededSteps++
    }
    return neededSteps
}

val calculateStepsToReachEndOnAllPaths: (List<String>) -> Long = { inputLines ->
    val movementStepsAndPaths = parseMovementStepsAndPathsFromInputLines(inputLines)
    calculateRequiredStepsWithMovementsStepsAndGivenPathsSimultaneously(
        movementStepsAndPaths.first,
        movementStepsAndPaths.second
    )
}

fun calculateRequiredStepsWithMovementsStepsAndGivenPathsSimultaneously(
    movementSteps: List<Char>,
    paths: MutableMap<String, Pair<String, String>>
): Long {
    val currentLocations = paths.filter { it.key.endsWith("A") }.map { it.key }

    val neededStepPerStartLocation = mutableListOf<Long>()

    //calculate steps for each start location and detect circular movement
    currentLocations.forEach { start ->
        var currentLocation = start
        var neededSteps = 0L
        val countSteps = movementSteps.size
        var nextStepIndex = 0
        var endLocationReachedAfterStepsWithIndex = listOf<Pair<String,Long>>()

        while (true) {
            if (nextStepIndex == countSteps) {
                nextStepIndex = 0
            }
            currentLocation = makeStep(currentLocation, movementSteps[nextStepIndex], paths)
            nextStepIndex++
            neededSteps++
            if (currentLocation.endsWith("Z")){
                endLocationReachedAfterStepsWithIndex = endLocationReachedAfterStepsWithIndex.plus(Pair(currentLocation, neededSteps))
                neededSteps = 0L
            }
            val detectCircleSteps = endLocationReachedAfterStepsWithIndex.groupByTo(HashMap(), { it.first }, { it.second })
            if(detectCircleSteps.any { it.value.size > 2 }){
                detectCircleSteps.forEach { neededStepPerStartLocation.add(it.value.first()) }
                break
            }
        }
    }
    return findLeastCommonMultipleOfNumbers(neededStepPerStartLocation)
}

fun findLeastCommonMultipleOfNumbers(numbers: List<Long>): Long {
    return numbers.reduce { acc, number -> findLeastCommonMultiple(acc, number) }
}

fun findLeastCommonMultiple(firstNumber: Long, secondNumber: Long): Long {
    val largerNumber = maxOf(firstNumber, secondNumber)
    val maximumPossibleLCM = firstNumber * secondNumber

    return generateSequence(largerNumber) { it + largerNumber }
        .first { it % firstNumber == 0L && it % secondNumber == 0L }
        .coerceAtMost(maximumPossibleLCM)
}