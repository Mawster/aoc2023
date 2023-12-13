import kotlin.system.measureTimeMillis

fun main() {
    solveDay12Part(
        "Part01: Calculate the possible arrangement for broken springs",
        "example_input",
        "example_result",
        "input",
        calculatePossibleArrangementForBrokenSprings
    )
    solveDay12Part(
        "Part02: Calculate the possible arrangement for broken springs folded 5",
        "example_input2",
        "example_result2",
        "input",
        calculatePossibleArrangementForBrokenSpringsFolded
    )
}

fun solveDay12Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 12)
    val exampleResult = readInput(exampleResultFileName, 12)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 12)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val calculatePossibleArrangementForBrokenSprings: (List<String>) -> Long = { brokenSpringLines ->
    brokenSpringLines.sumOf { calculateArrangements(it.split(" ")[0], it.split(" ")[1].split(",").map { it.toInt() }) }
}

val calculatePossibleArrangementForBrokenSpringsFolded: (List<String>) -> Long = { brokenSpringLines ->
    brokenSpringLines.sumOf { calculateArrangements(it.split(" ")[0], it.split(" ")[1].split(",").map { it.toInt() }, 5) }
}

fun calculateArrangements(
    conditions: String,
    requiredDamaged: List<Int>,
    foldFactor: Int = 1
): Long {
    var arrangements = 0L
    val unfoldedRequiredDamaged = List(foldFactor){requiredDamaged}.flatten()
    val validatorRegex = generateCheckerRegex(unfoldedRequiredDamaged)
    val unfoldedCondition = conditions.repeat(foldFactor)

    val unknownSpringIndices = unfoldedCondition.mapIndexed { index, char -> if (char == '?') index else null }.filterNotNull()
    var binaryStringIndex = 0
    var binaryString = "0"
    while(binaryString.replace("1","").isNotEmpty()){
        binaryString = binaryStringIndex.toString(2).padStart(unknownSpringIndices.size, '0')
        var binaryStringCounter = 0
        val checkConditionBuilder = StringBuilder()
        unfoldedCondition.forEachIndexed { index, c ->
            if (index in unknownSpringIndices) {
                if(binaryString[binaryStringCounter] == '1') {
                    checkConditionBuilder.append("#")
                } else {
                    checkConditionBuilder.append(".")
                }
                binaryStringCounter++
            } else {
                checkConditionBuilder.append(c)
            }
        }
        if (validatorRegex.matches(checkConditionBuilder.toString())) {
            arrangements++
            println(checkConditionBuilder.toString())
        }
        binaryStringIndex++

    }
    return arrangements
}

fun generateCheckerRegex(damagedSprings: List<Int>): Regex {
    val regexBuilder = StringBuilder()
    regexBuilder.append("(")
    damagedSprings.forEachIndexed { index, damagedSpring ->
        if (index == 0) {
            regexBuilder.append("[.]*")
        }
        regexBuilder.append("#{$damagedSpring}")
        regexBuilder.append("[.]")
        if (index < damagedSprings.size - 1) {
            regexBuilder.append("+")
        } else {
            regexBuilder.append("*")
        }
    }
    regexBuilder.append(")$")

    return Regex(regexBuilder.toString())
}