package kr.co.infomark.soundmasking.model

import java.math.BigDecimal

data class MaskingGetParameterDTO(var cmd : String,
                                  var result : String,

                                  var gg_momentum : BigDecimal,
                                  var gg_pstep : BigDecimal,
                                  var gg_nstep : BigDecimal,
                                  var gg_pclip : BigDecimal,

                                  var gg_nclip : BigDecimal,
                                  var eq_step : BigDecimal,
                                  var eq_clip : BigDecimal
                                  )
