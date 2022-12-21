package kr.co.infomark.soundmasking.model

data class SystemInfoResultModel(
    var cmd : String,
    var result : String,
    var manufacturer : String,
    var brand : String,
    var character : String,
    var model : String,
    var serialno : String,
    var revision : String
)
