package com.mcw.rides.generic.modules

class MessageModule {

    fun format(msg: String): String {
        return "§6§lMCW-Rides §8► §7$msg§7."
    }

    fun error(err: String): String {
        return "§6§lMCW-Rides §8► §c$err§c."
    }

}