package com.mcw.rides.editor.objects.train

data class Train(
        var section: Int,
        var size: Int,
        var length: Int,
        var model: Int,
        var seats: List<Seat> = mutableListOf()
)