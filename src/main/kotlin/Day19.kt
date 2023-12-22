import kotlin.system.measureTimeMillis

fun main() {
//    solveDay19Part(
//        "Part01: Run parts through workflows",
//        "example_input",
//        "example_result",
//        "input",
//        runPartsThroughWorkflow
//    )
    solveDay19Part(
        "Part02: Calculate distinct combinations of category ratings",
        "example_input2",
        "example_result2",
        "input",
        calculateDistinctCombinationsOfCategoryRatings
    )
}

fun solveDay19Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 19)
    val exampleResult = readInput(exampleResultFileName, 19)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        println("Calculated example Result: $calculatedExampleResult")
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 19)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val workflows = mutableMapOf<String, Workflow>()

val runPartsThroughWorkflow: (List<String>) -> Long = { workflowInput: List<String> ->
    val workflowStepRegex = Regex("(\\w*)\\{(.*)}")
    workflowInput.takeWhile { it.isNotEmpty() }.map { workflowDescription ->
        workflowStepRegex.find(workflowDescription)?.let { matchResult ->
            val workflowName = matchResult.groupValues[1]
            val workflowStepDescription = matchResult.groupValues[2]
            val workflowStepParts = workflowStepDescription.split(",")
            val workflowSteps = workflowStepParts.takeWhile {it.contains('>') || it.contains('<')}.map {  workflowStep ->
                val workflowStepParts = workflowStep.split(":")
                val category = workflowStepParts[0].first()
                val checkFunction = if (workflowStepParts[0][1] == '>') {
                    checkBigger
                } else {
                    checkSmaller
                }
                val valueToCompareTo = workflowStepParts[0].takeLastWhile { it != '>' && it != '<' }.toLong()
                val nextWorkflowName = workflowStepParts[1]
                WorkflowStep(category, checkFunction, valueToCompareTo, nextWorkflowName)
            }
            workflows[workflowName] = Workflow(workflowSteps, workflowStepParts.last())
        }
    }

    val categoryRatingExtractorRegex = Regex("\\w=(\\d*)")
    workflowInput.takeLastWhile { it.isNotEmpty() }.sumOf {categoryRatingLine ->
        val categoryValues = categoryRatingExtractorRegex.findAll(categoryRatingLine).toList().map { it.groupValues[1].toLong() }
        val result = workflows["in"]!!.processInput(mapOf('x' to categoryValues[0], 'm' to categoryValues[1], 'a' to categoryValues[2], 's' to categoryValues[3]))
        if(result == "A") {
            categoryValues[0] + categoryValues[1] + categoryValues[2] + categoryValues[3]
        } else {
            0L
        }
    }
}

val calculateDistinctCombinationsOfCategoryRatings: (List<String>) -> Long = { workflowInput: List<String> ->
    val workflowStepRegex = Regex("(\\w*)\\{(.*)}")
    workflowInput.takeWhile { it.isNotEmpty() }.map { workflowDescription ->
        workflowStepRegex.find(workflowDescription)?.let { matchResult ->
            val workflowName = matchResult.groupValues[1]
            val workflowStepDescription = matchResult.groupValues[2]
            val workflowStepParts = workflowStepDescription.split(",")
            val workflowSteps = workflowStepParts.takeWhile {it.contains('>') || it.contains('<')}.map {  workflowStep ->
                val workflowStepParts = workflowStep.split(":")
                val category = workflowStepParts[0].first()
                val checkFunction = if (workflowStepParts[0][1] == '>') {
                    checkBigger
                } else {
                    checkSmaller
                }
                val valueToCompareTo = workflowStepParts[0].takeLastWhile { it != '>' && it != '<' }.toLong()
                val nextWorkflowName = workflowStepParts[1]
                WorkflowStep(category, checkFunction, valueToCompareTo, nextWorkflowName)
            }
            workflows[workflowName] = Workflow(workflowSteps, workflowStepParts.last())
        }
    }

    var possibleCombinations = 0L
    for(x in 1..4000){
        for(m in 1..4000){
            for(a in 1..4000){
                for(s in 1..4000){
                    val result = workflows["in"]!!.processInput(mapOf('x' to x.toLong(), 'm' to m.toLong(), 'a' to a.toLong(), 's' to s.toLong()))
                    if(result == "A") {
                        possibleCombinations++
                    }
                }
            }
        }
    }
    possibleCombinations
}

val checkBigger: (value: Long, valueToCompareTo: Long) -> Boolean = { value, valueToCompareTo ->
    value > valueToCompareTo
}

val checkSmaller: (value: Long, valueToCompareTo: Long) -> Boolean = { value, valueToCompareTo ->
    value < valueToCompareTo
}

class Workflow(private val steps: List<WorkflowStep>, private val backupWorkflowName: String) {
    fun processInput(categoryRatingInput: Map<Char, Long>): String {
        val successfulStep = steps.firstOrNull { it.processInput(categoryRatingInput) != null }
        if(successfulStep == null) {
            return when(backupWorkflowName) {
                "A" -> "A"
                "R" -> "R"
                else -> workflows[backupWorkflowName]!!.processInput(categoryRatingInput)
            }
        }
        return when(successfulStep.nextWorkflowName) {
            "A" -> "A"
            "R" -> "R"
            else -> workflows[successfulStep.nextWorkflowName]!!.processInput(categoryRatingInput)
        }
    }
}

class WorkflowStep(
    private val category: Char,
    val checkFunction: (value: Long, valueToCompareTo: Long) -> Boolean,
    private val valueToCompareTo: Long, val nextWorkflowName: String
) {
    fun processInput(categoryRatingInput: Map<Char, Long>): String? {
        return if (checkFunction(categoryRatingInput[category]!!, valueToCompareTo)) {
            nextWorkflowName
        } else {
            null
        }
    }
}
