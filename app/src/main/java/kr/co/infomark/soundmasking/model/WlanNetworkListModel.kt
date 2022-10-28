package kr.co.infomark.soundmasking.model

data class WlanNetworkListModel(var result : String, var data : MutableList<Data>){
    data class Data(var ssid : String, var status : String)
}
