package com.mcw.rides.generic.modules

import org.bukkit.entity.Player
import java.util.*

class PlayerModule {

    private val playerMap = HashMap<UUID, Player>()

    fun register(player: Player) { playerMap[player.uniqueId] = player }
    fun register(players: Collection<Player?>) { for (player in players) { register(player!!) } }
    fun remove(player: Player) { playerMap.remove(player.uniqueId) }

    fun getPlayer(uuid: UUID?): Player? { return playerMap[uuid] }
    fun getPlayers(): Collection<Player?>? { return playerMap.values }

}