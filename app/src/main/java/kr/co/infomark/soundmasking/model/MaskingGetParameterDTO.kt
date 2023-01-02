package kr.co.infomark.soundmasking.model

data class MaskingGetParameterDTO(var cmd : String,
                                  var result : String,
                                  var gg_pstep : String,
                                  var gg_nstep : String,
                                  var gg_pclip : String,

                                  var gg_nclip : String,
                                  var eq_step : String,
                                  var eq_clip : String
                                  )
