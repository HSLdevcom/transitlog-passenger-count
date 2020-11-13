package fi.hsl.passengercount.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Count(

        @JsonProperty("class") val _class : String,
        @JsonProperty("in") val _in : Int,
        @JsonProperty("out") val _out : Int
)