package fi.hsl.passengercount.model

import java.util.*

data class APC(
    val desi : String,
    val dir : String,
    val oper : Int,
    val veh : Int,
    val tst : Date,
    val tsi : Long,
    val lat : Double,
    val long : Double,
    val odo : Double,
    val oday : String,
    val jrn : Int,
    val line : Int,
    val start : String,
    val loc : String,
    val stop : String,
    val route : String,
    val vehiclecounts : Vehiclecounts
)