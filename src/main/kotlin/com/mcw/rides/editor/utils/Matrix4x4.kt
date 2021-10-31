package com.mcw.rides.editor.utils

import org.bukkit.util.Vector

class Matrix4x4 {

    var m00 = 0.0
    var m01 = 0.0
    var m02 = 0.0
    var m03 = 0.0
    var m10 = 0.0
    var m11 = 0.0
    var m12 = 0.0
    var m13 = 0.0
    var m20 = 0.0
    var m21 = 0.0
    var m22 = 0.0
    var m23 = 0.0
    var m30 = 0.0
    var m31 = 0.0
    var m32 = 0.0
    var m33 = 0.0

    constructor(m00: Double, m01: Double, m02: Double, m03: Double, m10: Double, m11: Double, m12: Double, m13: Double, m20: Double, m21: Double, m22: Double, m23: Double, m30: Double, m31: Double, m32: Double, m33: Double) {
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m03 = m03
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
        this.m13 = m13
        this.m20 = m20
        this.m21 = m21
        this.m22 = m22
        this.m23 = m23
        this.m30 = m30
        this.m31 = m31
        this.m32 = m32
        this.m33 = m33
    }

    constructor()

    operator fun set(m00: Double, m01: Double, m02: Double, m03: Double, m10: Double, m11: Double, m12: Double, m13: Double, m20: Double, m21: Double, m22: Double, m23: Double, m30: Double, m31: Double, m32: Double, m33: Double) {
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m03 = m03
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
        this.m13 = m13
        this.m20 = m20
        this.m21 = m21
        this.m22 = m22
        this.m23 = m23
        this.m30 = m30
        this.m31 = m31
        this.m32 = m32
        this.m33 = m33
    }

    fun getRotation(): Quaternion {
        val tr = m00 + m11 + m22
        return if (tr > 0.0) {
            Quaternion(m21 - m12, m02 - m20, m10 - m01, 1.0 + tr)
        } else if (m00 > m11 && m00 > m22) {
            Quaternion(1.0 + m00 - m11 - m22, m01 + m10, m02 + m20, m21 - m12)
        } else {
            if (m11 > m22) Quaternion(m01 + m10, 1.0 + m11 - m00 - m22, m12 + m21, m02 - m20) else Quaternion(m02 + m20, m12 + m21, 1.0 + m22 - m00 - m11, m10 - m01)
        }
    }

    fun fromColumns3x3(v0: Vector, v1: Vector, v2: Vector): Matrix4x4? {
        return Matrix4x4(v0.x, v1.x, v2.x, 0.0, v0.y, v1.y, v2.y, 0.0, v0.z, v1.z, v2.z, 0.0, 0.0, 0.0, 0.0, 1.0)
    }

}