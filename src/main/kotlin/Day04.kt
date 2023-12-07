import kotlin.math.min
import kotlin.math.pow

fun main() {
    solveDay04Part(
        "Part01: Get points of scratchcards",
        "example_input",
        "example_result",
        "input",
        calculatePointsOfCards
    )
    solveDay04Part(
        "Part02: Calculate total count of cards in stack",
        "example_input2",
        "example_result2",
        "input",
        calculateTotalSizeOfCardStack
    )
}

fun solveDay04Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Int
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 4)
    val exampleResult = readInput(exampleResultFileName, 4)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    check(calculatedExampleResult == exampleResult.first().toInt())
    println("Calculated example is right! Result: $calculatedExampleResult")

    println("Calculating result for given input file $challengeInputFileName with solver function")
    val challengeInput = readInput(challengeInputFileName, 4)
    val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
    println("Calculated challenge result: $challengeResult")
}

val calculatePointsOfCards: (List<String>) -> Int = { cards ->
    val cardStack = generateCardStackBasedOnInput(cards)
    cardStack.sumOf { 2.toDouble().pow(it.wonNumbers - 1).toInt() }
}

val calculateTotalSizeOfCardStack: (List<String>) -> Int = { cards ->
    val cardStack = generateCardStackBasedOnInput(cards)
    cardStack.sumOf { it.solveCardWithStack(cardStack) }
}

fun generateCardStackBasedOnInput(inputCards: List<String>): List<Card> {
    val numberExtractorRegex = Regex("\\d+")
    return inputCards.mapIndexed { index, cardString ->
        cardString.substringAfter(":").split("|").let { cardNumbers ->
            Card(
                index,
                numberExtractorRegex.findAll(cardNumbers[0]).map { it.value.toInt() }.toSet(),
                numberExtractorRegex.findAll(cardNumbers[1]).map { it.value.toInt() }.toSet()
            )
        }
    }
}

data class Card(val number: Int, val winningNumbers: Set<Int>, val myNumbers: Set<Int>) {
    private var cardValue = 0
    val wonNumbers = winningNumbers.intersect(myNumbers).size

    fun solveCardWithStack(cardStack: List<Card>): Int {
        if (cardValue == 0) {
            cardValue = if (wonNumbers == 0) {
                1
            } else {
                cardStack.subList(number + 1, min(number + wonNumbers + 1, cardStack.size))
                    .sumOf { it.solveCardWithStack(cardStack) } + 1
            }
        }
        return cardValue
    }
}

