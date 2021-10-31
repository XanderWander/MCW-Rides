package com.mcw.rides.editor.objects.track

data class Section(
        var start: Int, var end: Int,
        var type: String,
        var stopPosition: Int, var breakSpeed: Double, var rollingSpeed: Double
)