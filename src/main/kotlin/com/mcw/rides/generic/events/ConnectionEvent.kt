package com.mcw.rides.generic.events

import com.mcw.rides.Main
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ConnectionEvent: Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {

        com.mcw.rides.Main.instance?.playerModule?.register(event.player)

    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {

        com.mcw.rides.Main.instance?.playerModule?.remove(event.player)

    }

}