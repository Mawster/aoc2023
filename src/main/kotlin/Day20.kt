import kotlin.system.measureTimeMillis

fun main() {
    solveDay20Part(
        "Part01: Calculate impulse count",
        "example_input",
        "example_result",
        "input",
        calculateImpulseCount
    )
}

fun solveDay20Part(
    partName: String,
    exampleInputFileName: String,
    exampleResultFileName: String,
    challengeInputFileName: String,
    twoDimensionalArraySolverFunction: (List<String>) -> Long
) {
    println("Start solving of \"$partName\"")

    println("Checking example with given solver")
    val exampleInput = readInput(exampleInputFileName, 20)
    val exampleResult = readInput(exampleResultFileName, 20)
    val calculatedExampleResult = twoDimensionalArraySolverFunction(exampleInput)
    var calculcationTime = measureTimeMillis {
        println("Calculated example Result: $calculatedExampleResult")
        check(calculatedExampleResult == exampleResult.first().toLong())
        println("Calculated example is right! Result: $calculatedExampleResult")
    }
    println("Calculation time: $calculcationTime ms")

    calculcationTime = measureTimeMillis {
        println("Calculating result for given input file $challengeInputFileName with solver function")
        val challengeInput = readInput(challengeInputFileName, 20)
        val challengeResult = twoDimensionalArraySolverFunction(challengeInput)
        println("Calculated challenge result: $challengeResult")
    }
    println("Calculation time: $calculcationTime ms")
}

val moduleConfiguration = mutableMapOf<String, Module>()
val buttonPressRXInputCycle = mutableMapOf<Long, List<SignalType>>()

val calculateImpulseCount: (List<String>) -> Long = { moduleConfigurationLines: List<String> ->
    moduleConfiguration.clear()
    val moduleToReceivers = mutableMapOf<String, String>()
    moduleConfigurationLines.forEach { moduleConfigurationLine ->
        moduleConfigurationLine.replace(" ", "").split("->").let {
            moduleToReceivers[it[0]] = it[1]
        }
    }

    moduleToReceivers.forEach { moduleToReceiverEntry ->
        val moduleId = moduleToReceiverEntry.key.substring(1)
        val newModule = moduleConfiguration.getOrPut(moduleId) {
            when (moduleToReceiverEntry.key[0]) {
                '%' -> FlipFlopModule(moduleId)
                '&' -> ConjunctionModule(moduleId)
                else -> BroadCastModule(moduleId)
            }
        }

        moduleToReceiverEntry.value.split(",").forEach { receiverModuleId ->
            val receiverModule =
                moduleConfiguration.getOrPut(receiverModuleId) {
                    moduleToReceivers.keys.find { it.substring(1) == receiverModuleId }.let {
                        if(it == null) {
                            BroadCastModule(receiverModuleId)
                        } else {
                            when (it[0]) {
                                '%' -> FlipFlopModule(receiverModuleId)
                                '&' -> ConjunctionModule(receiverModuleId)
                                else -> BroadCastModule(receiverModuleId)
                            }
                        }
                    }
                }
            receiverModule.addInputModule(newModule)
            newModule.addReceiverModule(receiverModule)
        }
    }

    var lowImpulsesSend = 0L
    var highImpulsesSend = 0L
    var buttonPressed = 0L
    while (true) {
        buttonPressed ++
        val signalCommandsToHandle = mutableListOf(
            SignalCommand("", SignalType.LOW, listOf(moduleConfiguration.values.first { it.moduleId == "roadcaster" }))
        )
        while (signalCommandsToHandle.isNotEmpty()) {
            val signalToHandle = signalCommandsToHandle.removeAt(0)
            signalToHandle.receiverModules.forEach { module ->
                if(module.moduleId == "rx" && signalToHandle.signal == SignalType.LOW){
                    println("Button pressed: $buttonPressed")
                }
                when (signalToHandle.signal) {
                    SignalType.HIGH -> highImpulsesSend++
                    SignalType.LOW -> lowImpulsesSend++
                    else -> {
                    }
                }
                signalCommandsToHandle.add(
                    module.handleSignalFromModuleWithId(
                        signalToHandle.signal,
                        signalToHandle.senderModuleId
                    )
                )
            }
        }
        buttonPressRXInputCycle[buttonPressed] = moduleConfiguration["gf"]!!.inputModules.values.toList()
        if(buttonPressRXInputCycle[buttonPressed]!!.contains(SignalType.HIGH)) {
            println(buttonPressRXInputCycle[buttonPressed])
        }
    }
    lowImpulsesSend*highImpulsesSend
}

//queue of modules to send signal to

interface Module {
    var moduleId: String
    val receiverModules: MutableList<Module>
    val inputModules: MutableMap<String, SignalType>
    fun handleSignalFromModuleWithId(signal: SignalType, senderModuleId: String): SignalCommand

    fun addReceiverModule(module: Module) {
        receiverModules.add(module)
    }

    fun addInputModule(module: Module) {
        inputModules[module.moduleId] = SignalType.LOW
    }
}

data class SignalCommand(val senderModuleId: String, val signal: SignalType, val receiverModules: List<Module>)

class BroadCastModule(override var moduleId: String) : Module {
    override val receiverModules = mutableListOf<Module>()
    override val inputModules = mutableMapOf<String, SignalType>()
    override fun handleSignalFromModuleWithId(
        signal: SignalType,
        senderModuleId: String
    ): SignalCommand {
        return SignalCommand(moduleId, signal, receiverModules)
    }
}

class FlipFlopModule(override var moduleId: String) : Module {
    private var currentState = SignalType.LOW
    override val receiverModules = mutableListOf<Module>()
    override val inputModules = mutableMapOf<String, SignalType>()
    override fun handleSignalFromModuleWithId(
        signal: SignalType,
        senderModuleId: String
    ): SignalCommand {
        if (signal == SignalType.LOW) {
            currentState = currentState.getInverseSignalType()
            return SignalCommand(moduleId, currentState, receiverModules)
        }
        return SignalCommand(moduleId, SignalType.NONE, listOf())
    }
}

class ConjunctionModule(override var moduleId: String) : Module {
    private var currentState = SignalType.LOW
    override val receiverModules = mutableListOf<Module>()
    override val inputModules = mutableMapOf<String, SignalType>()
    override fun handleSignalFromModuleWithId(
        signal: SignalType,
        senderModuleId: String
    ): SignalCommand {
        inputModules[senderModuleId] = signal

        currentState = if (inputModules.values.all { it == SignalType.HIGH }) {
            SignalType.LOW
        } else {
            SignalType.HIGH
        }

        return SignalCommand(moduleId, currentState, receiverModules)
    }
}

enum class SignalType {
    HIGH, LOW, NONE;

    fun getInverseSignalType(): SignalType {
        return when (this) {
            HIGH -> LOW
            LOW -> HIGH
            NONE -> NONE
        }
    }
}


