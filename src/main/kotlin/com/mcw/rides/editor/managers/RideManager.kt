package com.mcw.rides.editor.managers

import com.google.gson.reflect.TypeToken
import com.mcw.rides.Main
import com.mcwalibi.rides.editor.objects.editor.RideList
import com.mcwalibi.rides.generic.modules.MessageModule
import com.mcwalibi.rides.generic.modules.StorageManager
import org.bukkit.entity.Player
import java.io.File

class RideManager(private val main: com.mcw.rides.Main) {

    private fun storageManager(): StorageManager {
        return main.storageManager
    }

    private val messageModule = MessageModule()

    private fun rideList(): List<String> {
        val type = object : TypeToken<RideList>() {}.type
        val rideList: RideList = storageManager().getJson(type) as RideList
        return rideList.rides
    }

    fun createRide(player: Player, name: String) {

        val ride = name.toLowerCase()

        if (ride == "data" || rideList().contains(ride)) { player.sendMessage("This ride name has already been used.") }
        else {

            val list = storageManager().getJsonRideList()
            list.rides = list.rides.plus(ride)
            storageManager().saveToJson("data", list)

            player.sendMessage(messageModule.format("Created ride §e§o$ride"))

        }

    }

    fun editRide(player: Player, name: String) {
        val ride = name.toLowerCase()

        if (ride == "data" || !rideList().contains(ride)) { player.sendMessage("This ride does not exist.") }
        else {

            com.mcw.rides.Main.instance.editorManager.editor(player, name)

            player.sendMessage(messageModule.format("Editing ride: §e§o$ride"))

        }

    }

    fun deleteRide(player: Player, name: String) {
        val ride = name.toLowerCase()

        if (ride == "data" || !rideList().contains(ride)) { player.sendMessage("This ride does not exist.") }
        else {

            com.mcw.rides.Main.instance.editorManager.stopEditor(ride)

            val list = storageManager().getJsonRideList()
            list.rides = list.rides.minus(ride)
            storageManager().saveToJson("data", list)

            val file = File("plugins/${main.name}/$ride")
            file.deleteRecursively()

            player.sendMessage(messageModule.format("Deleted ride: §e§o$ride"))

        }

    }

    fun listRide(player: Player) {

        player.sendMessage(messageModule.format("Rides: §e§o${rideList().toString().replace("[", "").replace("]", "")}"))

    }

}