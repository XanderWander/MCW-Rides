package com.mcw.rides.editor.utils

import org.bukkit.util.Vector

class Quaternion {

    var x = 0.0
    var y = 0.0
    var z = 0.0
    var w = 0.0

    constructor(x: Double, y: Double, z: Double, w: Double) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        this.normalize()
    }

    constructor()

    private fun normalize() {
        val f = getNormalizationFactor(x, y, z, w)
        x *= f
        y *= f
        z *= f
        w *= f
    }

    private fun getNormalizationFactor(x: Double, y: Double, z: Double, w: Double): Double {
        return getNormalizationFactorLS(x * x + y * y + z * z + w * w)
    }

    fun getNormalizationFactorLS(lengthSquared: Double): Double {
        return if (Math.abs(1.0 - lengthSquared) < 2.107342E-8) 2.0 / (1.0 + lengthSquared) else 1.0 / Math.sqrt(lengthSquared)
    }


    fun fromLookDirection(dir: Vector, up: Vector): Quaternion? {
        val d = dir.clone().normalize()
        val s = up.clone().crossProduct(dir).normalize()
        val u = d.clone().crossProduct(s)
        val matrix4x4 = Matrix4x4()
        val result: Quaternion = matrix4x4.fromColumns3x3(s, u, d)?.getRotation() ?: return Quaternion(0.0, 0.0, 0.0, 0.0)
        return if (java.lang.Double.isNaN(result.x)) fromLookDirection(dir) else result
    }

    private fun fromLookDirection(dir: Vector): Quaternion? {
        val q: Quaternion = Quaternion(-dir.y, dir.x, 0.0, dir.z + dir.length())
        if (java.lang.Double.isNaN(q.w)) {
            q.x = 0.0
            q.y = 1.0
            q.z = 0.0
            q.w = 0.0
        }
        return q
    }
    
}