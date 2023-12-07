import kotlin.system.measureTimeMillis

fun main() {
    solveDay05Part(
        "Part01: Get lowest location for seeds",
        "example_input",
        "example_result",
        "input",
        calculateLowestLocationOfSeeds
    )
    solveDay05Part(
        "Part01: Get lowest location for ranges of seeds",
        "example_input2",
        "example_result2",
        "input",
        calculateLowestLocationOfSeedsRanges
    )
}

fun solveDay05Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Int
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 5)
    val exampleResult = readInput(exampleResultFileName, 5)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    check(calculatedExampleResult == exampleResult.first().toInt())
    println("Calculated example is right! Result: $calculatedExampleResult")

    println("Calculating result for given input file $challengeInputFileName with solver function")
    val challengeInput = readInput(challengeInputFileName, 5)
    val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
    println("Calculated challenge result: $challengeResult")
}

val calculateLowestLocationOfSeeds: (List<String>) -> Int = { almanac ->
    val seedExtractorRegex = Regex("(\\d+)")
    val mapExtractorRegex = Regex(".*\\n(.*\\d+.*)")

    //preprocess input
    val seeds = almanac[0].substringAfter("seeds: ").split(" ").map { Seed(it.toLong()) }
    val indexesOfMapBeginn = almanac.mapIndexedNotNull { index, line -> index.takeIf { line.contains("map") } }

    val maps = mutableMapOf<MapType, List<RangeDefinition>>()

    indexesOfMapBeginn.forEachIndexed { index, mapBeginnIndex ->
        var currentRangeLine = mapBeginnIndex + 1
        val rangesForMapType = mutableListOf<RangeDefinition>()
        val nextMapLineIndex =
            if (indexesOfMapBeginn.size > index + 1) indexesOfMapBeginn[index + 1] - 1 else almanac.size
        while (currentRangeLine < nextMapLineIndex) {
            almanac[currentRangeLine].split(" ").map { it.toLong() }.let {
                rangesForMapType.add(RangeDefinition(it[0], it[1], it[2]))
            }
            currentRangeLine++
        }
        maps[MapType.values()[index]] = rangesForMapType
    }

    println("Initial seeds")
    maps.forEach { (type, rangeDefinitions) ->
        println("Processing $type")
        rangeDefinitions.forEach { rangeDefinition ->
            seeds.filter { it.initialLocation in (rangeDefinition.sourceRange..<rangeDefinition.sourceRange + rangeDefinition.rangeLength) }
                .forEach {
                    if (!it.mappedPhases.containsKey(type)) {
                        val diffToSource = it.initialLocation - rangeDefinition.sourceRange

                        it.mappedPhases[type] = rangeDefinition.destinationRange + diffToSource
                        it.initialLocation = rangeDefinition.destinationRange + diffToSource
                    }
                }
            println(seeds)
        }
        seeds.forEach {
            if (!it.mappedPhases.containsKey(type)) {
                it.mappedPhases[type] = it.initialLocation
            }
        }
    }

    println(seeds.minOf { it.mappedPhases[MapType.HUMIDITYLOCATION]!! })
    seeds.minOf { it.mappedPhases[MapType.HUMIDITYLOCATION]!!.toInt() }
}

val calculateLowestLocationOfSeedsRanges: (List<String>) -> Int = { almanac ->
    //preprocess input
    val seedNumbers = almanac[0].substringAfter("seeds: ").split(" ").map { it.toLong() }
    var minimalSeed: Seed? = null
    val seedPairs = seedNumbers.size.div(2)


    val indexesOfMapBeginn = almanac.mapIndexedNotNull { index, line -> index.takeIf { line.contains("map") } }

    val maps = mutableMapOf<MapType, List<RangeDefinition>>()

    indexesOfMapBeginn.forEachIndexed { index, mapBeginnIndex ->
        var currentRangeLine = mapBeginnIndex + 1
        val rangesForMapType = mutableListOf<RangeDefinition>()
        val nextMapLineIndex =
            if (indexesOfMapBeginn.size > index + 1) indexesOfMapBeginn[index + 1] - 1 else almanac.size
        while (currentRangeLine < nextMapLineIndex) {
            almanac[currentRangeLine].split(" ").map { it.toLong() }.let {
                rangesForMapType.add(RangeDefinition(it[0], it[1], it[2]))
            }
            currentRangeLine++
        }
        maps[MapType.values()[index]] = rangesForMapType
    }


    println("Initial seeds")
    for (i in 0 until seedPairs + 1 step 2) {
        val elapsed = measureTimeMillis {
            (seedNumbers[i]..<seedNumbers[i] + seedNumbers[i + 1]).forEach { seedInitialLocation ->
                val currentSeed = Seed(seedInitialLocation)
                maps.forEach { (type, rangeDefinitions) ->
//                    println("Processing $type")
                    rangeDefinitions.forEach { rangeDefinition ->
                        if (currentSeed.initialLocation in (rangeDefinition.sourceRange..<rangeDefinition.sourceRange + rangeDefinition.rangeLength)) {
                            if (!currentSeed.mappedPhases.containsKey(type)) {
                                val diffToSource = currentSeed.initialLocation - rangeDefinition.sourceRange

                                currentSeed.mappedPhases[type] = rangeDefinition.destinationRange + diffToSource
                                currentSeed.initialLocation = rangeDefinition.destinationRange + diffToSource
                            }
                        }
                    }
                    if (!currentSeed.mappedPhases.containsKey(type)) {
                        currentSeed.mappedPhases[type] = currentSeed.initialLocation
                    }
                }
                if (minimalSeed == null || currentSeed.mappedPhases[MapType.HUMIDITYLOCATION]!! < minimalSeed!!.mappedPhases[MapType.HUMIDITYLOCATION]!!) {
                    minimalSeed = currentSeed
                }
            }
        }
        println("Processed seed pair in ${elapsed / 1000} seconds!")
    }


    println(minimalSeed!!.mappedPhases[MapType.HUMIDITYLOCATION]!!)
    minimalSeed!!.mappedPhases[MapType.HUMIDITYLOCATION]!!.toInt()
}

enum class MapType {
    SEEDSOIL,
    SOILFERTILIZER,
    FERTILIZERWATER,
    WATERLIGHT,
    LIGHTTEMPERATURE,
    TEMPERATUREHUMIDITY,
    HUMIDITYLOCATION
}

data class RangeDefinition(val destinationRange: Long, val sourceRange: Long, val rangeLength: Long)

data class Seed(var initialLocation: Long) {
    var mappedPhases = mutableMapOf<MapType, Long>()
}