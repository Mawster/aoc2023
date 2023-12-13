import kotlin.system.measureTimeMillis

fun main() {
    solveDay13Part(
        "Part01: Calculate reflections in patterns",
        "example_input",
        "example_result",
        "input",
        calculateReflectionsInPatterns
    )
    solveDay13Part(
        "Part02: Calculate reflections in patterns with smudge fix",
        "example_input2",
        "example_result2",
        "input",
        calculateReflectionsInPatternsWithSmudgeFix
    )
}

fun solveDay13Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 13)
    val exampleResult = readInput(exampleResultFileName, 13)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 13)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val calculateReflectionsInPatterns: (List<String>) -> Long = { reflectionLines ->
    var summaryReflections = 0L

    val patterns = getPatternsFromReflectionLines(reflectionLines)

    for (pattern in patterns) {
        //Check for vertical reflections
        for (i in 0 until pattern[0].length - 1) {
            if (doesPatternHavePerfectReflectionAtStartIndex(pattern, i, CheckType.HORIZONTAL)) {
                summaryReflections += (i + 1)
            }
        }

        //check for horizontal reflections
        for (i in 0 until pattern.size - 1) {
            if (doesPatternHavePerfectReflectionAtStartIndex(pattern, i, CheckType.VERTICAL)) {
                summaryReflections += (i + 1) * 100
            }
        }
    }
    summaryReflections
}

val smudgeFixTrackerVerticalIndices = mutableListOf<Int>()
val smudgeFixTrackerHorizontalIndices = mutableListOf<Int>()
val calculateReflectionsInPatternsWithSmudgeFix: (List<String>) -> Long = { reflectionLines ->
    var summaryReflections = 0L

    val patterns = getPatternsFromReflectionLines(reflectionLines)

    for (pattern in patterns) {
        var patternReflectionValue = 0L
        for (i in 0 until pattern[0].length - 1) {
            smudgeFixTrackerHorizontalIndices.clear()
            if (doesPatternHavePerfectReflectionAtStartIndex(pattern, i, CheckType.HORIZONTAL, smudgeFixEnabled = true)) {
                if(!smudgeFixTrackerHorizontalIndices.contains(i) && patternReflectionValue == 0L){
                    patternReflectionValue = (i + 1).toLong()
                }
                if(smudgeFixTrackerHorizontalIndices.contains(i)){
                    patternReflectionValue = (i + 1).toLong()
                }
            }
        }

        for (i in 0 until pattern.size - 1) {
            smudgeFixTrackerVerticalIndices.clear()
            if (doesPatternHavePerfectReflectionAtStartIndex(pattern, i, CheckType.VERTICAL, smudgeFixEnabled = true)) {
                if(!smudgeFixTrackerVerticalIndices.contains(i) && patternReflectionValue == 0L){
                    patternReflectionValue = (i + 1) * 100L
                }
                if(smudgeFixTrackerVerticalIndices.contains(i)){
                    patternReflectionValue = (i + 1) * 100L
                }
            }
        }
        summaryReflections += patternReflectionValue
    }
    summaryReflections
}


fun getPatternsFromReflectionLines(reflectionLines: List<String>): List<List<String>> {
    val patterns = mutableListOf<List<String>>()
    val reflectionLineIterator = reflectionLines.iterator()
    do {
        val pattern = mutableListOf<String>()
        do {
            val patternLine = reflectionLineIterator.next()
            if (patternLine.isEmpty()) {
                break
            } else {
                pattern.add(patternLine)
            }
        } while (reflectionLineIterator.hasNext())
        patterns.add(pattern)
    } while (reflectionLineIterator.hasNext())
    return patterns
}

fun doesPatternHavePerfectReflectionAtStartIndex(
    pattern: List<String>,
    startIndex: Int,
    checkType: CheckType,
    smudgeFixEnabled: Boolean = false
): Boolean {
    return if (checkType == CheckType.HORIZONTAL) {
        List(pattern.size) { index ->
            hasExactReflectionWithStartingPoints(
                pattern,
                startIndex,
                startIndex + 1,
                index,
                checkType,
                smudgeFixEnabled,
                startIndex
            )
        }
            .all { it }
    } else {
        pattern[startIndex].mapIndexed { index, _ ->
            hasExactReflectionWithStartingPoints(
                pattern,
                startIndex,
                startIndex + 1,
                index,
                checkType,
                smudgeFixEnabled,
                startIndex
            )
        }
            .all { it }
    }
}

fun hasExactReflectionWithStartingPoints(
    pattern: List<String>,
    checkLeft: Int,
    checkRight: Int,
    lineColumnIndex: Int,
    checkType: CheckType,
    smudgeFixEnabled: Boolean = false,
    rowColumnProcess: Int = 0
): Boolean {
    if (checkLeft < 0 ||
        (checkRight >= pattern.size && checkType == CheckType.VERTICAL) ||
        (checkRight >= pattern[0].length && checkType == CheckType.HORIZONTAL)
    ) {
        return true
    }
    if(checkType == CheckType.HORIZONTAL){
        if(pattern[lineColumnIndex][checkLeft] == pattern[lineColumnIndex][checkRight]){
            return hasExactReflectionWithStartingPoints(pattern, checkLeft - 1, checkRight + 1, lineColumnIndex, checkType, smudgeFixEnabled, rowColumnProcess)
        }else if(smudgeFixEnabled && !smudgeFixTrackerHorizontalIndices.contains(rowColumnProcess)){
            smudgeFixTrackerHorizontalIndices.add(rowColumnProcess)
            return hasExactReflectionWithStartingPoints(pattern, checkLeft - 1, checkRight + 1, lineColumnIndex, checkType, smudgeFixEnabled, rowColumnProcess)
        }
    } else if (checkType == CheckType.VERTICAL){
        if(pattern[checkLeft][lineColumnIndex] == pattern[checkRight][lineColumnIndex]){
            return hasExactReflectionWithStartingPoints(pattern, checkLeft - 1, checkRight + 1, lineColumnIndex, checkType, smudgeFixEnabled, rowColumnProcess)
        }else if(smudgeFixEnabled && !smudgeFixTrackerVerticalIndices.contains(rowColumnProcess)){
            smudgeFixTrackerVerticalIndices.add(rowColumnProcess)
            return hasExactReflectionWithStartingPoints(pattern, checkLeft - 1, checkRight + 1, lineColumnIndex, checkType, smudgeFixEnabled, rowColumnProcess)
        }
    }
    return false
}

enum class CheckType {
    HORIZONTAL, VERTICAL
}