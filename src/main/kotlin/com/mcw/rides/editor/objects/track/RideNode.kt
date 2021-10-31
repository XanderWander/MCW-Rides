package com.mcw.rides.editor.objects.track

import com.mcwalibi.rides.editor.objects.editor.EditorNode

data class RideNode(
        var locX: Double, var locY: Double, var locZ: Double,
        var yaw: Double = 0.0, var pitch: Double = 0.0, var roll: Double = 0.0,
) {
    constructor(node: EditorNode): this(node.locX, node.locY, node.locZ, node.yaw, node.pitch, node.roll)
}