package kr.co.infomark.soundmasking.model

data class WlanScanResultModel(var result : String, var data : MutableList<Data>){
    data class Data(var ssid : String, var rssi : Int)
}
