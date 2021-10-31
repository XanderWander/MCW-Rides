package com.mcw.rides.editor.events

import com.mcw.rides.Main
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class SwapHandItems : Listener {

    @EventHandler
    fun onSwapHandItems(event: PlayerSwapHandItemsEvent) {

        com.mcw.rides.Main.instance!!.editorManager.onSwap(event)

    }

}