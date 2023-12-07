import kotlin.math.pow
import kotlin.system.measureTimeMillis

fun main() {
    solveDay07Part(
        "Part01: Calculate total winnings of hands",
        "example_input",
        "example_result",
        "input",
        calculateTotalWinningsOfHands
    )
    solveDay07Part(
        "Part02: Calculate total winnings of hands with jokers",
        "example_input2",
        "example_result2",
        "input",
        calculateTotalWinningsOfHandsWithJokers
    )
}

fun solveDay07Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 7)
    val exampleResult = readInput(exampleResultFileName, 7)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    val calculcationTime = measureTimeMillis {
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    println("Calculating result for given input file $challengeInputFileName with solver function")
    val challengeInput = readInput(challengeInputFileName, 7)
    val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
    println("Calculated challenge result: $challengeResult")
}

val calculateTotalWinningsOfHands: (List<String>) -> Long = { handsToBidsLines ->
    val handValueToBid = mutableListOf<Pair<Long, Long>>()

    handsToBidsLines.forEach { handToBid ->
        val parsedLine = handToBid.split(" ")
        val bidValue = parsedLine[1].toLong()

        val cards = parsedLine[0].groupingBy { it }.eachCount()
        val cardsValue = parsedLine[0].mapIndexed { index, c -> c.getSingleCardValue() * 10.toDouble().pow(8-(2*index))   }.sum().toLong()
        val handTypeValue = determineHandTypeValue(cards)

        handValueToBid.add(Pair(cardsValue + handTypeValue, bidValue))
    }
    handValueToBid.sortBy { it.first }
    handValueToBid.mapIndexed { index, pair -> (index+1) * pair.second }.sum()
}

val calculateTotalWinningsOfHandsWithJokers: (List<String>) -> Long = { handsToBidsLines ->
    val handValueToBid = mutableListOf<Pair<Long, Pair<Long,String>>>()

    handsToBidsLines.forEach { handToBid ->
        val parsedLine = handToBid.split(" ")
        val bidValue = parsedLine[1].toLong()

        val cardsValue = calculateHandCardsValue(parsedLine[0])
        val handTypeValue = determineHandTypeValueWithJokerCards(parsedLine[0])

        handValueToBid.add(Pair(cardsValue + handTypeValue, Pair(bidValue, parsedLine[0])))
    }
    handValueToBid.sortBy { it.first }
    handValueToBid.forEach { println(it) }
    handValueToBid.mapIndexed { index, pair -> (index+1) * pair.second.first }.sum()
}

fun determineHandTypeValueWithJokerCards(cardsString: String): Long {
    val cards = cardsString.groupingBy { it }.eachCount()
    val filteredOutJokerCards = cards.filter { it.key != 'J' }
    val jokerCardsCount = cards.filter { it.key == 'J' }.values.sum()
    return if (filteredOutJokerCards.size == 1 || filteredOutJokerCards.isEmpty()) {
        100000000000
    } else if (filteredOutJokerCards.any { it.value == 4 } || filteredOutJokerCards.any { it.value + jokerCardsCount  == 4 }) {
        90000000000
    } else if (filteredOutJokerCards.any { it.value == 3 }  && filteredOutJokerCards.any { it.value == 2 }) {
        80000000000
    } else if(filteredOutJokerCards.any { it.value + jokerCardsCount  == 3 } && jokerCardsCount == 1 && filteredOutJokerCards.values.all { it == 2 }){
        80000000000
    } else if (filteredOutJokerCards.any { it.value == 3 } || filteredOutJokerCards.any { it.value + jokerCardsCount  == 3 }) {
        70000000000
    } else if (cards.filter { it.value == 2 }.size == 2) {
        60000000000
    } else if (filteredOutJokerCards.any { it.value == 2 } || filteredOutJokerCards.any { it.value + jokerCardsCount  == 2 }) {
        50000000000
    } else {
        40000000000
    }
}

fun calculateHandCardsValue(cardString: String): Long {
    return cardString.mapIndexed { index, c -> c.getSingleCardValueJokered() * 10.toDouble().pow(8-(2*index))   }.sum().toLong()
}

fun Char.getSingleCardValueJokered(): Int {
    return when (this) {
        'T' -> 10
        'J' -> 1
        'Q' -> 12
        'K' -> 13
        'A' -> 14
        else -> this.digitToInt()
    }
}

fun determineHandTypeValue(cards: Map<Char, Int>): Long {
    return if (cards.size == 1) {
        100000000000
    } else if (cards.any { it.value == 4 }) {
        90000000000
    } else if (cards.any { it.value == 3 } && cards.any { it.value == 2 }) {
        80000000000
    } else if (cards.any { it.value == 3 }) {
        70000000000
    } else if (cards.filter { it.value == 2 }.size == 2) {
        60000000000
    } else if (cards.any { it.value == 2 }) {
        50000000000
    } else {
        40000000000
    }
}

fun Char.getSingleCardValue(): Int {
    return when (this) {
        'T' -> 10
        'J' -> 11
        'Q' -> 12
        'K' -> 13
        'A' -> 14
        else -> this.digitToInt()
    }
}
