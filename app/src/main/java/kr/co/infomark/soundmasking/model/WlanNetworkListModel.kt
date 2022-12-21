package kr.co.infomark.soundmasking.model

data class WlanNetworkListModel(var cmd : String = "", var result : String = "", var data : MutableList<Data> = arrayListOf()){
    data class Data(var ssid : String, var bssid : String, var rssi : Int, var status : String)
}
