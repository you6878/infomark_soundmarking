package kr.co.infomark.soundmasking.model

data class StateModel(
    var state : String = "",
    var timestamp : Long = 0,
    var gain : Double = 0.0,
    var bands : MutableList<Band> = mutableListOf()
){
    data class Band(var band:Int, var gain : Double)
}