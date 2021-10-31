package com.mcw.rides.editor.objects.editor

data class EditorNode(
        var locX: Double, var locY: Double, var locZ: Double,
        var yaw: Double = 0.0, var pitch: Double = 0.0, var roll: Double = 0.0,
        var radius: Double = 3.0, var invert: Boolean = false,
        var connections: List<Int> = mutableListOf()
)