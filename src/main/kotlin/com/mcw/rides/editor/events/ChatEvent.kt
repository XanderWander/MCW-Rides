package com.mcw.rides.editor.events

import com.mcw.rides.Main
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatEvent : Listener {

    @EventHandler
    fun onClick(event: AsyncPlayerChatEvent) {

       // event.isCancelled = true

        com.mcw.rides.Main.instance!!.editorManager.onChat(event)

    }
}