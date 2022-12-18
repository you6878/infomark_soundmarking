package kr.co.infomark.soundmasking.model


var WlanState = "wlan_state"
var WlanScanResult = "wlan_scan_result"
var WlanAddNetwork = "wlan_add_network"
var WlanRemoveNetwork = "wlan_remove_network"
var WlanNetworkList = "wlan_network_list"
var MaskingGetParameter = "masking_get_parameter"
var MaskingSetParameter = "masking_set_parameter"
var MaskingDisable = "masking_disable"

var MaskingEnable = "masking_enable"
var MaskingState = "masking_state"




data class CommandModel(var cmd: String, var ssid : String? = null, var password : String? = null)
data class RemoveCommandModel(var cmd: String, var ssid : String)
