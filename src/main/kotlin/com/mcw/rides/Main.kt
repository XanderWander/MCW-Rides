package com.mcw.rides

import com.mcw.rides.editor.events.ClickEvent
import com.mcw.rides.editor.managers.EditorManager
import com.mcw.rides.editor.managers.RideManager
import com.mcw.rides.editor.commands.CommandRide
import com.mcw.rides.editor.events.ChatEvent
import com.mcw.rides.editor.events.SwapHandItems
import com.mcw.rides.generic.events.ConnectionEvent
import com.mcw.rides.generic.modules.MessageModule
import com.mcw.rides.generic.modules.PlayerModule
import com.mcw.rides.generic.modules.StorageManager
import com.mcw.rides.generic.utils.ItemUtil
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {

    companion object {
        lateinit var instance: com.mcw.rides.Main
    }

    val playerModule = PlayerModule()
    val storageManager = StorageManager(this)
    val rideManager = RideManager(this)
    val editorManager = EditorManager(this)
    val itemUtils = ItemUtil()
    val messageModule = MessageModule()

    override fun onEnable() {

        com.mcw.rides.Main.Companion.instance = this

        registerEvents()
        registerCommands()

        playerModule.register(Bukkit.getOnlinePlayers())

        logger.info("$name enabled.")

    }

    override fun onDisable() {

        editorManager.stopEditors("plugin disabling")

        logger.info("$name disabled.")

    }

    private fun registerEvents() {

        Bukkit.getPluginManager().registerEvents(ClickEvent(), this)
        Bukkit.getPluginManager().registerEvents(ConnectionEvent(), this)
        Bukkit.getPluginManager().registerEvents(ChatEvent(), this)
        Bukkit.getPluginManager().registerEvents(SwapHandItems(), this)

    }

    private fun registerCommands() {

        this.getCommand("ride")?.setExecutor(com.mcw.rides.editor.commands.CommandRide())

    }

}