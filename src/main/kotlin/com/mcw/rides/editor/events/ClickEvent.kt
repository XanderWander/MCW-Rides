package com.mcw.rides.editor.events

import com.mcw.rides.Main
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ClickEvent : Listener {

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {

//        val player = event.player
//
//        if (Main.instance.panelHandler.panelOperators.containsKey(player)) {
//
//            val panel = Main.instance.panelHandler.panelOperators[player]!!
//            val precision = 10
//            val pitch = -player.location.pitch / 180 * PI
//            val yaw = -(player.location.yaw + 90) / 180 * PI
//            val x = cos(pitch) * cos(yaw) / precision
//            val y = sin(pitch) / precision
//            val z = cos(pitch) * sin(-yaw) / precision
//            val pos = event.player.location.add(0.0, if (player.isSneaking) 1.27 else 1.62, 0.0)
//
//            for (i in 0..400) {
//                pos.add(x, y, z)
//
//                if (Main.instance.panelHitBox.isInHitBox(pos, Main.instance.panelList.panels[panel].loc, 4) != -1) {
//                    player.sendMessage("wohoo")
//                    return
//                }
//            }
//
//        }

        com.mcw.rides.Main.instance.editorManager.onClick(event)

    }

}