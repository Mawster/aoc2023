import kotlin.system.measureTimeMillis

fun main() {
    solveDay09Part(
        "Part01: Calculate next number of sequence",
        "example_input",
        "example_result",
        "input",
        caclulateSumOfNextNumbersOfSequences
    )
    solveDay09Part(
        "Part02: Calculate next number of sequence recursively",
        "example_input2",
        "example_result2",
        "input",
        caclulateSumOfNextNumbersOfSequencesRecursive
    )
}

fun solveDay09Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 9)
    val exampleResult = readInput(exampleResultFileName, 9)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 9)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val caclulateSumOfNextNumbersOfSequences: (List<String>) -> Long = { inputLines ->
    inputLines.sumOf {historyValueLine ->
        predictNextValueForHistoryValues(historyValueLine.split(" ").map { it.toInt() })
    }.toLong()
}

val caclulateSumOfNextNumbersOfSequencesRecursive: (List<String>) -> Long = { inputLines ->
    inputLines.sumOf {historyValueLine ->
        predictNextValueForHistoryValues(historyValueLine.split(" ").map { it.toInt() }, predictionType = PredictionType.BACKWARDS)
    }.toLong()
}

private fun predictNextValueForHistoryValues(historyValues: List<Int>, predictionType: PredictionType = PredictionType.FORWARD): Int {
    if(historyValues.isEmpty()) return 0
    return when(predictionType){
        PredictionType.FORWARD -> historyValues.last() + predictNextValueForHistoryValues(historyValues.zipWithNext { a, b -> b - a })
        PredictionType.BACKWARDS -> historyValues.first() - predictNextValueForHistoryValues(historyValues.zipWithNext { a, b -> b - a }, PredictionType.BACKWARDS)
    }
}

enum class PredictionType {
    FORWARD,
    BACKWARDS
}