package fi.hsl.passengercount.model

data class DoorCount (
    val door : String,
    val count : List<Count>
)