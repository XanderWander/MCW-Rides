package com.mcw.rides.editor.commands

import com.mcw.rides.Main
import com.mcwalibi.rides.editor.utils.Quaternion
import com.mcwalibi.rides.editor.utils.RenderUtils
import com.mcwalibi.rides.editor.utils.StandCreator
import com.mcwalibi.rides.generic.utils.ItemUtil
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.*

class CommandRide: CommandExecutor {

    private val stands = arrayListOf<ArmorStand>()

    private val standCreator = StandCreator()
    private val renderUtils = RenderUtils()
    private val itemUtils = ItemUtil()

    private var runRide: com.mcw.rides.editor.commands.RunRide? = null

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender is Player) {

            val rideManager = com.mcw.rides.Main.instance.rideManager

            when (args.size) {

                0 -> sender.help("")

                1 -> when (args[0].toLowerCase()) {
                    "create" -> sender.usage("ride create <name>", "Please provide a ride name.")
                    "edit" -> sender.usage("ride edit <name>", "Please provide a ride name.")
                    "delete" -> sender.usage("ride delete <name>", "Please provide a ride name.")
                    "render" -> render()
                    "load" -> run()
                    "start" -> sender.sendMessage("Not implemented")
                    "stop" -> stop()
                    "list" -> rideManager.listRide(sender)
                    else -> sender.help("Unknown arguments.")
                }

                2 -> when (args[0].toLowerCase()) {
                    "create" -> rideManager.createRide(sender, args[1])
                    "edit" -> rideManager.editRide(sender, args[1])
                    "delete" -> rideManager.deleteRide(sender, args[1])
                    "render" -> sender.sendMessage("Not implemented")
                    "load" -> sender.sendMessage("Not implemented")
                    "stop" -> sender.sendMessage("Not implemented")
                    "list" -> rideManager.listRide(sender)
                    else -> sender.help("Unknown arguments.")
                }

                else -> sender.help("Too many arguments.")

            }

        } else {

            sender.sendMessage("This command is only available for players.")

        }

        return false

    }

    private fun remove() {

        for (stand in stands) {
            stand.remove()
        }
        stands.clear()

    }

    private fun run() {

        val reader = Files.newBufferedReader(Paths.get("plugins/Rides/joris1.csv"))
        val csvParser = CSVParser(reader, CSVFormat.DEFAULT.withQuote(null).withNullString(""))
        val location = Location(Bukkit.getWorld("world"), -562.5, 88.5, -67.5)

        val locations = arrayListOf<Location>()
        val dir = arrayListOf<Vector>()
        val up = arrayListOf<Vector>()

        for ((count, csvRecord) in csvParser.withIndex()) {
            if (count == 0)
                continue

            val name = csvRecord.get(0)
            val data = name.split("\t")

            var x = 0.0; var y = 0.0; var z = 0.0
            var fx = 0.0; var fy = 0.0; var fz = 0.0
            var ux = 0.0; var uy = 0.0; var uz = 0.0

            for ((index, value) in data.withIndex()) {
                when(index) {
                    1 -> x = value.toDouble()
                    2 -> y = value.toDouble()
                    3 -> z = value.toDouble()
                    4 -> fx = value.toDouble()
                    5 -> fy = value.toDouble()
                    6 -> fz = value.toDouble()
                    10 -> ux = value.toDouble()
                    11 -> uy = value.toDouble()
                    12 -> uz = value.toDouble()
                }
            }

            val newLoc = Location(location.world, location.x + x, location.y + y, location.z + z)

            locations.add(newLoc)
            dir.add(Vector(fx, fy, fz))
            up.add(Vector(ux, uy, uz))

            if (Vector(fx, fy, fz) != Vector(fx, fy, fz).normalize()) {
                Bukkit.broadcastMessage("Forward vector at $count might be wrong")
            }

//            if (ux == 0.0 || uy == 0.0 || uz == 0.0) {
//                Bukkit.broadcastMessage("Upward vector at $count might be wrong")
//            }

//            if (count % 100 == 0) {
//                Bukkit.broadcastMessage("---------------")
//                Bukkit.broadcastMessage("$newLoc")
//                Bukkit.broadcastMessage("---------------")
//            }

        }

        runRide = com.mcw.rides.editor.commands.RunRide(locations, dir, up)
    }

    private fun stop() {
        runRide?.stop()
        runRide = null
    }

    private fun render() {

        //fun fromLookDirection(dir: Vector, up: Vector): Quaternion? {

        val reader = Files.newBufferedReader(Paths.get("plugins/Rides/joris1.csv"))
        val csvParser = CSVParser(reader, CSVFormat.DEFAULT.withQuote(null).withNullString(""))
        val location = Location(Bukkit.getWorld("world"), -562.5, 88.5, -67.5)

        for ((count, csvRecord) in csvParser.withIndex()) {
            if (count == 0)
                continue
            if (count % 66 != 0)
                continue
            //if (count == 100)
                //return

            val name = csvRecord.get(0)
            val data = name.split("\t")

            var x = 0.0; var y = 0.0; var z = 0.0

            var fx = 0.0; var fy = 0.0; var fz = 0.0
            var ux = 0.0; var uy = 0.0; var uz = 0.0

            for ((index, value) in data.withIndex()) {
                when(index) {
                    1 -> x = value.toDouble()
                    2 -> y = value.toDouble()
                    3 -> z = value.toDouble()
                    4 -> fx = value.toDouble()
                    5 -> fy = value.toDouble()
                    6 -> fz = value.toDouble()
                    10 -> ux = value.toDouble()
                    11 -> uy = value.toDouble()
                    12 -> uz = value.toDouble()
                }
            }

            val dir = Vector(fx, fy, fz)
            val up = Vector(ux, uy, uz)



            val newLoc = Location(location.world, location.x + x, location.y + y, location.z + z)
            val angle = getCorrectArmorStandAngle(dir, up)

            stands.add(standCreator.create(newLoc, visible = false, head = angle, helmet = itemUtils.createItem(Material.BIRCH_TRAPDOOR, "")))

            Bukkit.broadcastMessage("---------------")
            Bukkit.broadcastMessage("$newLoc")
            Bukkit.broadcastMessage("---------------")
        }

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

    private fun Player.help(error: String) {

        this.sendMessage("§8§m---------§r§8{ §cMCW-Rides §7V0.1 §8}§m---------")
        this.sendMessage("")
        if (error != "") {
            this.sendMessage("§c $error")
            this.sendMessage("")
        }
        this.sendMessage("§7 ► §e/ride create <name>")
        this.sendMessage("§7 ► §e/ride edit <name>")
        this.sendMessage("§7 ► §e/ride delete <name>")
        this.sendMessage("§7 ► §e/ride list")
        this.sendMessage("§7 ► §e/ride render <name>")
        this.sendMessage("§7 ► §e/ride load <name>")
        this.sendMessage("§7 ► §e/ride start <name>")
        this.sendMessage("§7 ► §e/ride stop <name>")
        this.sendMessage("")
        this.sendMessage("§8§m---------------------------------")

    }

    private fun Player.usage(usage: String, info: String) {

        this.sendMessage("§8§m---------§r§8{ §cMCW-Rides §7V0.1 §8}§m---------")
        this.sendMessage("")
        this.sendMessage("§c $info")
        this.sendMessage("")
        this.sendMessage("§7 ► §e/$usage")
        this.sendMessage("")
        this.sendMessage("§8§m---------------------------------")

    }

}