import kotlin.system.measureTimeMillis

fun main() {
    solveDay15Part(
        "Part01: Calculate hash sum of initialization sequence",
        "example_input",
        "example_result",
        "input",
        calculateHashSumOfInitializationSequence
    )
    solveDay15Part(
        "Part02: Calculate focus power of lense configuration",
        "example_input2",
        "example_result2",
        "input",
        calculateFocusPowerOfLenseConfiguration
    )
}

fun solveDay15Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 15)
    val exampleResult = readInput(exampleResultFileName, 15)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        println("Calculated example Result: $calculatedExampleResult")
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 15)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val calculateHashSumOfInitializationSequence: (List<String>) -> Long = { initializationSequenceInput: List<String> ->
    initializationSequenceInput[0].split(",").sumOf { initSequence ->
        calculateHashSumOfString(initSequence)
    }
}

fun calculateHashSumOfString(string: String): Long {
    return string.fold(0L) { currentValue, char ->
        (currentValue + char.code) * 17 % 256
    }
}

val calculateFocusPowerOfLenseConfiguration: (List<String>) -> Long = { initializationSequenceInput: List<String> ->
    val sequenceExtractorRegex = Regex("(\\D+)([-=])(\\d*)")
    val boxConfiguration = mutableMapOf<Int, MutableList<String>>()
    initializationSequenceInput[0]
        .split(",")
        .map { sequenceExtractorRegex.matchEntire(it)!!.groupValues }
        .forEach {
            val boxNumber = calculateHashSumOfString(it[1]).toInt()
            if(it[2] == "-"){
                boxConfiguration.getOrDefault(boxNumber, mutableListOf()).removeIf { boxContent -> boxContent.contains(it[1]) }
            }else if(it[2] == "="){
                val currentBoxContent = boxConfiguration.getOrPut(boxNumber) { mutableListOf() }
                val indexOfElement = currentBoxContent.indexOfFirst { boxContent -> boxContent.contains(it[1]) }
                if(indexOfElement != -1){
                    currentBoxContent[indexOfElement] = "${it[1]} ${it[3]}"
                }else {
                    currentBoxContent.add("${it[1]} ${it[3]}")
                }
            }
        }

    boxConfiguration.entries.sumOf { box ->
        box.value.withIndex().sumOf { (lenseIndex, lense) ->
            (1+box.key) * (1+lenseIndex) * lense.split(" ")[1].toLong()
        }
    }
}