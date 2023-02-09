import effectiveinjection.Inject
import effectiveinjection.AutoWire

class CoffeeMaker {
    @Inject
    lateinit var heater: Heater
    @Inject lateinit var grinder: Grinder

//    init {
//        CoffeeMakerInjector.inject(this)
//    }
}

@AutoWire
class Heater
@AutoWire
class Grinder