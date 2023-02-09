package some.example

import effectiveinjection.Inject
import effectiveinjection.AutoWire

class CoffeeMaker {
    @Inject lateinit var heater: Heater
    @Inject lateinit var grinder: Grinder

    init {
        CoffeeMakerInjector.inject(this)
    }

    fun makeCoffee() {
        heater.heat()
        grinder.grind()
        println("Coffee done")
    }
}

class WaterHeater {
    @Inject lateinit var heater: Heater

    init {
        WaterHeaterInjector.inject(this)
    }

    fun heat() {
        heater.heat()
        println("Water done")
    }
}

class Heater {
    fun heat() {
        println("Heating water")
    }
}
class Grinder {
    fun grind() {
        println("Grinding")
    }
}

fun main() {
    val coffeeMaker = CoffeeMaker()
    coffeeMaker.makeCoffee()
    // Heating water
    // Grinding
    // Coffee done
    val waterHeater = WaterHeater()
    waterHeater.heat()
    // Heating water
    // Water done
}