import kotlin.system.measureTimeMillis

fun main() {
    solveDay06Part(
        "Part01: Get numbers to beat speed boot record of multiple races",
        "example_input",
        "example_result",
        "input",
        calculateNumbersToBeatSpeedBootRecords
    )
    solveDay06Part(
        "Part02: Get numbers to beat speed boot record of one big fat race",
        "example_input2",
        "example_result2",
        "input",
        calculateNumbersToBeatFatRaceSpeedBootRecord
    )
}

fun solveDay06Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 5)
    val exampleResult = readInput(exampleResultFileName, 5)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    val calculcationTime = measureTimeMillis {
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    println("Calculating result for given input file $challengeInputFileName with solver function")
    val challengeInput = readInput(challengeInputFileName, 5)
    val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
    println("Calculated challenge result: $challengeResult")
}

val calculateNumbersToBeatSpeedBootRecords: (List<String>) -> Long = { raceSchedule ->
    val raceTimes = raceSchedule[0].substringAfter(":").trim().split(Regex("\\s+")).map { it.toLong() }
    val records = raceSchedule[1].substringAfter(":").trim().split(Regex("\\s+")).map { it.toLong() }

    raceTimes.zip(records).map { Race(it.first, it.second) }.map { race: Race ->
        val startHoldTime = race.time / 2
        val withLowerHoldTime = calculateNumbersOfWaysToBeatRecordInRace(startHoldTime, -1, race)
        val withHigherHoldTime = calculateNumbersOfWaysToBeatRecordInRace(startHoldTime + 1, 1, race)
        withHigherHoldTime + withLowerHoldTime
    }.reduce(Long::times)
}

val calculateNumbersToBeatFatRaceSpeedBootRecord: (List<String>) -> Long = { raceSchedule ->
    val raceTime = raceSchedule[0].substringAfter(":").trim().split(Regex("\\s+")).reduce(String::plus).toLong()
    val record = raceSchedule[1].substringAfter(":").trim().split(Regex("\\s+")).reduce(String::plus).toLong()

    val race = Race(raceTime, record)
    val startHoldTime = race.time / 2
    val withLowerHoldTime = calculateNumbersOfWaysToBeatRecordInRace(startHoldTime, -1, race)
    val withHigherHoldTime = calculateNumbersOfWaysToBeatRecordInRace(startHoldTime + 1, 1, race)
    withHigherHoldTime + withLowerHoldTime
}

fun calculateNumbersOfWaysToBeatRecordInRace(holdTime: Long, step: Int, race: Race): Long {
    val moveTime = race.time - holdTime
    return if (holdTime * moveTime > race.distance) {
        calculateNumbersOfWaysToBeatRecordInRace(holdTime + step, step, race) + 1
    } else {
        0
    }
}

data class Race(val time: Long, val distance: Long)