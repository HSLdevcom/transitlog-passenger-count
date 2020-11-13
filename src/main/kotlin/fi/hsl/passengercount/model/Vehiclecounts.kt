package fi.hsl.passengercount.model

data class Vehiclecounts(
    val countquality : String,
    val vehicleload : Int,
    val vehicleloadratio : Double,
    val doorcounts : List<DoorCount>,
    val extensions : String
)