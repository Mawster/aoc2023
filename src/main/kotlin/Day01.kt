fun main() {
    solveDay01Part(
        "Part01: Read calibration values based on digits",
        "example_input",
        "example_result",
        "input",
        getCalibrationValuesDigitBasedFromLine
    )
    solveDay01Part(
        "Part02: Read calibration values based on digits and digits formed of letters",
        "example_input2",
        "example_result2",
        "input",
        getCalibrationValuesDigitAndLetterBasedFromLine
    )
}

fun solveDay01Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    lineSolverFunction: (String) -> Int
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName,1)
    val exampleResult = readInput(exampleResultFileName,1)
    val calculatedExampleResult = exampleInput.sumOf(lineSolverFunction)
    check(calculatedExampleResult == exampleResult.first().toInt())
    println("Calculated example is right! Result: $calculatedExampleResult")

    println("Calculating result for given input file $challengeInputFileName with solver function")
    val challengeInput = readInput(challengeInputFileName,1)
    val challengeResult = challengeInput.sumOf(lineSolverFunction)
    println("Calculated challenge result: $challengeResult")
}

val getCalibrationValuesDigitBasedFromLine: (String) -> Int = { line ->
    "${line.find { it.isDigit() }}${line.findLast { it.isDigit() }}".toInt()
}

val digitStrings = listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine")

val getCalibrationValuesDigitAndLetterBasedFromLine: (String) -> Int = { line ->
    val resolvedDigitsMap = sortedMapOf<Int, Int>()
    line.forEachIndexed { index, c ->
        if (c.isDigit()) {
            resolvedDigitsMap[index] = c.digitToInt()
        }
    }
    line.findAnyOf(digitStrings)?.run {
        resolvedDigitsMap.put(this.first, this.second.convertLetterDigitToInt())
    }
    line.findLastAnyOf(digitStrings)?.run {
        resolvedDigitsMap.put(this.first, this.second.convertLetterDigitToInt())
    }
    "${resolvedDigitsMap.values.first()}${resolvedDigitsMap.values.last()}".toInt()
}

fun String.convertLetterDigitToInt(): Int {
    return when (this) {
        "one" -> 1
        "two" -> 2
        "three" -> 3
        "four" -> 4
        "five" -> 5
        "six" -> 6
        "seven" -> 7
        "eight" -> 8
        "nine" -> 9
        else -> throw IllegalStateException("String is not a valid digit expressed by letters!")
    }
}