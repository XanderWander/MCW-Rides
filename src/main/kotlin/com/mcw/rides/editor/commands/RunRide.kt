package com.mcw.rides.editor.commands

import com.mcw.rides.Main
import com.mcwalibi.rides.editor.utils.Quaternion
import com.mcwalibi.rides.editor.utils.RenderUtils
import com.mcwalibi.rides.editor.utils.StandCreator
import com.mcwalibi.rides.generic.utils.ItemUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.floor

class RunRide(private val locations: ArrayList<Location>, private val dir: ArrayList<Vector>, private val up: ArrayList<Vector>): BukkitRunnable() {

    private val standCreator = StandCreator()
    private val renderUtils = RenderUtils()
    private val itemUtils = ItemUtil()
    private var pos = 0
    private val speed = 20.0
    private var stand: ArmorStand = standCreator.create(gravity = false, location = locations[0], visible = false, head = getCorrectArmorStandAngle(dir[0], up[0]), helmet = itemUtils.createItem(Material.RED_CONCRETE, ""))

    init {
        Bukkit.broadcastMessage("Init")
        this.runTaskTimerAsynchronously(com.mcw.rides.Main.instance, 0L, 1L)
    }

    fun stop() {
        this.cancel()
        stand.remove()
    }

    override fun run() {

        Bukkit.broadcastMessage("Run")

        pos += floor(speed).toInt()

        if (pos >= locations.size)
            pos -= locations.size

        val angle = getCorrectArmorStandAngle(dir[pos], up[pos])

        stand.teleport(locations[pos])
        stand.headPose = angle

    }

    private fun getCorrectArmorStandAngle(dir: Vector, up: Vector): EulerAngle {
        val quaternion = Quaternion()
        val rotation: Quaternion = quaternion.fromLookDirection(dir, up)!!
        val qx: Double = rotation.x
        val qy: Double = rotation.y
        val qz: Double = rotation.z
        val qw: Double = rotation.w
        val rx = 1.0 + 2.0 * (-qy * qy - qz * qz)
        val ry = 2.0 * (qx * qy + qz * qw)
        val rz = 2.0 * (qx * qz - qy * qw)
        val uz = 2.0 * (qy * qz + qx * qw)
        val fz = 1.0 + 2.0 * (-qx * qx - qy * qy)
        return if (abs(rz) < 1.0 - 1E-15) {
            EulerAngle(atan2(uz, fz), asin(rz), atan2(-ry, rx))
        } else {
            val sign = if (rz < 0) -1.0 else 1.0
            EulerAngle(0.0, sign * 90.0, -sign * 2.0 * renderUtils.atanTwo(qx, qw))
        }
    }

}