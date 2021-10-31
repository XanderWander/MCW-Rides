package com.mcw.rides.editor.managers

import com.mcw.rides.Main
import com.mcwalibi.rides.editor.objects.editor.EditingPlayer
import com.mcwalibi.rides.editor.objects.editor.EditorData
import com.mcwalibi.rides.editor.objects.editor.EditorNode
import com.mcwalibi.rides.editor.objects.track.RideNode
import com.mcwalibi.rides.editor.utils.RenderUtils
import com.mcwalibi.rides.editor.utils.StandCreator
import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

class EditorManager(private val main: com.mcw.rides.Main) {

    private val editingPlayers: HashMap<Player, EditingPlayer> = HashMap()
    private val editorData: HashMap<String, EditorData> = HashMap()
    private val standCreator: StandCreator = StandCreator()

    /*
    Public functions
     */

    fun editor(player: Player, ride: String) {

        //player.world.spawnParticle(Particle.FALLING_DUST, player.location, 1, 0.0, 0.0, 0.0, Particle.DustOptions(Color.AQUA, 1F))

        if (player.isEditing()) {

            val editingPlayer = editingPlayers[player]!!

            switchInventory(player, false)
            editingPlayers.remove(player)
            main.storageManager.saveRide(ride, editorData[ride])

            editingPlayer.clearEntities()

            player.sendInfo("No longer editing")

        } else {

            val editingPlayer = EditingPlayer(ride)

            editingPlayers[player] = editingPlayer
            if (!editorData.containsKey(ride)) {
                val data: EditorData = main.storageManager.getFromJsonEditorData("$ride/data")
                editorData[ride] = data
            }
            updateEditor(player, editorData[ride]!!)
            switchInventory(player, true)

            player.sendInfo("Now editing: §e§o$ride")

        }

    }

    fun onClick(event: PlayerInteractEvent) {

        val player = event.player

        val clickedBlock = event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.LEFT_CLICK_BLOCK
        val mainHand = event.hand == EquipmentSlot.HAND
        val useBlock = event.useInteractedBlock() == Event.Result.ALLOW

        if (!player.isEditing()) return
        if ( (clickedBlock && !mainHand) || (clickedBlock && !useBlock) ) return
        if (player.inventory.heldItemSlot != 6) event.isCancelled = true

        val name = player.inventory.itemInMainHand.itemMeta?.displayName
        val rightClick = event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR
        val ride = editingPlayers[player]?.ride

        val editData = editorData(ride) ?: return

        when (name) {
            null -> return
            "§7Add Point" -> addPoint(player, editData)
            "§7Rem Point" -> remPoint(player, editData)
            "§7Invert" -> invert(player, editData)
            "§7Select Point" -> selPoint(player, editData, rightClick)
            "§7Connect Point" -> conPoint(player)

            "§7Yaw", "§7Pitch", "§7Roll", "§7Radius",
            "§7X", "§7Y", "§7Z" -> changeSetting(player, editData, name, rightClick)

            else -> return
        }

        if (name != "§7Connect Point") updateEditor(player, editData)
        if (name != "7Select Point") {
            if (editData.editorNodes.containsKey(editData.selectedNode)) {
                editData.editedNodes.plus(editData.selectedNode)
                for (id in editData.editorNodes[editData.selectedNode]!!.connections) {
                    if (!editData.editedNodes.contains(id)) editData.editedNodes.plus(id)
                }
            }
        }

    }

    fun onChat(event: AsyncPlayerChatEvent) {

        val player = event.player

        if (!player.isEditing()) return
        val name = player.inventory.itemInMainHand.itemMeta?.displayName
        val ride = editingPlayers[player]?.ride
        val editData = editorData(ride) ?: return

        Bukkit.getScheduler().runTask(main, Runnable {
            when (name) {
                "§7Select Point" -> selectPoint(player, editData, event.message)
                "§7Connect Point" -> connectPoint(player, editData, event.message)
            }
            updateEditor(player, editData)
        })

    }

    fun onSwap(event: PlayerSwapHandItemsEvent) {

        val player = event.player
        if (!player.isEditing()) return
        event.isCancelled = true
        changeItems(event.player)

    }

    fun stopEditors(reason: String) {

        for (editingPlayer in editingPlayers.keys) {

            val editingP = editingPlayers[editingPlayer]!!

            switchInventory(editingPlayer, false)
            editingPlayers.remove(editingPlayer)
            main.storageManager.saveRide(editingP.ride, editorData[editingP.ride])

            editingP.clearEntities()

            editingPlayer.sendInfo("§7Stopped editor, reason: §e§o$reason")

        }

        editingPlayers.clear()

    }

    fun stopEditor(editor: String) {

        for (editingPlayer in editingPlayers.keys) {

            val editingP = editingPlayers[editingPlayer]!!

            if (editingP.ride != editor) continue

            switchInventory(editingPlayer, false)
            editingPlayers.remove(editingPlayer)

            editingP.clearEntities()

            editingPlayer.sendInfo("§7Stopped editor, reason: §e§oRide deleted")

        }

    }

    /*
    Private functions
     */

    private fun editorData(data: String?): EditorData? {
        return editorData[data]
    }

    private fun addPoint(player: Player, editorData: EditorData) {

        val id = editorData.newNode
        editorData.newNode = id + 1

        val location = player.getTargetBlock(null, 100).location.add(0.5, 0.0, 0.5)

        val node = EditorNode(location.x, location.y, location.z)
        editorData.editorNodes[id] = node
        val selected = editorData.selectedNode
        if (editorData.editorNodes.containsKey(selected)) {
            connectPoint(player, editorData, id, selected, true)
            val node2 = editorData.editorNodes[selected]!!
            val yaw = atan2(node.locX - node2.locX, node.locZ - node2.locZ ) / PI * -180 + 90
            node.yaw = yaw
            editorData.editorNodes[id] = node
        }
        editorData.selectedNode = id
        player.sendInfo("Selected node: §e§o$id")

    }

    private fun remPoint(player: Player, editorData: EditorData) {
        var id = editorData.selectedNode
        if (editorData.editorNodes.containsKey(id)) {

            for (node in editorData.editorNodes[id]!!.connections) {
                connectPoint(player, editorData, id, node, false)
            }

            editorData.editorNodes.remove(id)
            if (editorData.newNode - 1 == id) editorData.newNode = id

            while (!editorData.editorNodes.containsKey(id)) {
                id--
                if (id <= 0) {
                    editorData.selectedNode = 0
                    return
                }
            }

            player.sendInfo("Now selected: §e§o$id")
            editorData.selectedNode = id

        } else {
            player.sendError("Selected node does not exist")
        }

    }

    private fun changeSetting(player: Player, editorData: EditorData, input: String, rightClick: Boolean) {
        val name = input.replace("§7", "").replace(" ", "").toLowerCase()
        val selected = editorData.selectedNode
        if (!editorData.editorNodes.containsKey(selected)) {
            player.sendError("Selected node does not exist")
            return
        }
        val sel = editorData.editorNodes[selected]!!
        val change: Double = if (rightClick) (if (player.isSneaking) 0.1 else 1.0) else (if (player.isSneaking) -0.1 else -1.0)
        var data = 0.0
        when (name) {
            "x" -> {
                sel.locX = sel.locX + change
                data = sel.locX
            }
            "y" -> {
                sel.locY = sel.locY + change
                data = sel.locY
            }
            "z" -> {
                sel.locZ = sel.locZ + change
                data = sel.locZ
            }
            "yaw" -> {
                sel.yaw = sel.yaw + change * 5
                data = sel.yaw
            }
            "pitch" -> {
                sel.pitch = sel.pitch + change * 5
                data = sel.pitch
            }
            "roll" -> {
                sel.roll = sel.roll + change * 5
                data = sel.roll
            }
            "radius" -> {
                sel.radius = sel.radius + change
                data = sel.radius
            }
        }

        val rounding = (data * 10).roundToInt()
        val rounded = rounding.toDouble() / 10

        player.sendInfo("Changed $name to §e§o$rounded")

    }

    private fun invert(player: Player, editorData: EditorData) {
        val node: EditorNode? = editorData.editorNodes[editorData.selectedNode]
        if (node != null) {
            node.invert = !node.invert
            player.sendInfo("Invert: §e§o${node.invert}")
        } else {
            player.sendError("Selected node does not exist")
        }
    }

    private fun selPoint(player: Player, editorData: EditorData, rightClick: Boolean) {
        val dir = if (rightClick) 1 else -1
        var selected = editorData.selectedNode.plus(dir)
        while (!editorData.editorNodes.keys.contains(selected)) {
            if (selected > 1000 || selected <= 0) {
                player.sendError("No nodes further")
                return
            }
            selected = selected.plus(dir)
        }
        editorData.selectedNode = selected
        player.sendInfo("Selected node: §e§o$selected")

    }

    private fun conPoint(player: Player) {
        changeItems(player)
    }

    private fun selectPoint(player: Player, editorData: EditorData, select: String) {
        try {
            val sel = Integer.parseInt(select)
            if (!editorData.editorNodes.containsKey(sel)) {
                player.sendError("Provided node does not exist")
                return
            }
            editorData.selectedNode = sel
            player.sendInfo("Selected node: §e§o$sel")
        } catch (e: NumberFormatException) {
            player.sendError("Please provide a number")
            return
        }
    }

    private fun connectPoint(player: Player, editorData: EditorData, connect: String) {

        try {
            val con = Integer.parseInt(connect)
            val selected = editorData.selectedNode

            if (!editorData.editorNodes.containsKey(selected)) {
                player.sendError("Selected node does not exist")
                return
            }
            if (!editorData.editorNodes.containsKey(con)) {
                player.sendError("Provided node does not exist")
                return
            }

            if (editorData.editorNodes[selected]!!.connections.contains(con)) {
                connectPoint(player, editorData, con, selected, false)
            } else {
                connectPoint(player, editorData, con, selected, true)
            }
        } catch (e: NumberFormatException) {
            player.sendError("Please provide a number")
            return
        }


    }

    private fun connectPoint(player: Player, editorData: EditorData, node1: Int, node2: Int, connect: Boolean) {

        val nodeCon = editorData.editorNodes[node1]!!
        val nodeSel = editorData.editorNodes[node2]!!
        if (connect) {
            nodeCon.connections = nodeCon.connections.plus(node2)
            nodeSel.connections = nodeSel.connections.plus(node1)
            player.sendInfo("Connecting nodes: [§e§o$node1§r§a>--<§e§o$node2§r§7]")
        } else {
            nodeCon.connections = nodeCon.connections.minus(node2)
            nodeSel.connections = nodeSel.connections.minus(node1)
            player.sendInfo("Disconnecting nodes: [§e§o$node1§r§c> §c <§e§o$node2§r§7]")
        }

    }

    /*
    Rendering track
     */

    private fun updateEditor(player: Player, editorData: EditorData) {

        val editingPlayer: EditingPlayer? = editingPlayers[player]
        val editorEntities: List<ArmorStand>? = editingPlayer?.editorEntities

        if (!editorEntities.isNullOrEmpty()) {
            for (stand in editingPlayers[player]?.editorEntities!!) { stand.remove() }
        }
        editingPlayers[player]?.editorEntities = ArrayList()

        for (node in editorData.editorNodes.entries) {
            renderNode(editorData, player, node.value, node.key)
            if (node.key != editorData.selectedNode) continue

            for (connection in node.value.connections) {
                val editNode: EditorNode = editorData.editorNodes[connection]!!
                renderConnection(player, editNode, node.value)
            }

        }

    }

    private fun renderConnection(player: Player, startNode: EditorNode, endNode: EditorNode) {

        val renderUtils = RenderUtils()

        val controlPoints: java.util.ArrayList<Location> = renderUtils.getControlPoints(startNode, endNode)

        val startRideNode = RideNode(startNode)
        val endRideNode = RideNode(endNode)
        var prev = startRideNode
        Bukkit.broadcastMessage("Defined pitch as ${prev.pitch}")

        val loc1 = Location(player.world, startNode.locX, startNode.locY, startNode.locZ)
        val loc2 = Location(player.world, endNode.locX, endNode.locY, endNode.locZ)
        val control1 = if (controlPoints[0].distance(loc2) < controlPoints[1].distance(loc2)) controlPoints[0] else controlPoints[1]
        val control2 = if (controlPoints[2].distance(loc1) < controlPoints[3].distance(loc1)) controlPoints[2] else controlPoints[3]
        val invert = startNode.invert && endNode.invert

        val bool1 = (controlPoints[2].distance(loc1) < controlPoints[3].distance(loc1))
        val bool2 = (controlPoints[0].distance(loc2) < controlPoints[1].distance(loc2))

        var xOld = loc1.x
        var yOld = loc1.y
        var zOld = loc1.z

        var rollStart: Double = if (bool1) startNode.roll else startNode.roll * -1
        var rollEnd: Double = if (bool2) endNode.roll * -1 else endNode.roll

        if ((bool1 && bool2) || !(bool1 || bool2)) {
            rollEnd *= -1
            rollStart *= -1
        }

        val rollDif = rollEnd - rollStart

        var xNew: Double
        var yNew: Double
        var zNew: Double
        var yaw: Double
        var pitch: Double
        var roll: Double
        var xDif: Double
        var yDif: Double
        var zDif: Double

        val density: Double = loc1.distance(loc2) * 3
        var t = 1 / density

        var i = 0
        while (i < density) {

            xNew = renderUtils.getFormula(t, loc1.x, loc2.x, control1.x, control2.x)
            yNew = renderUtils.getFormula(t, loc1.y, loc2.y, control1.y, control2.y)
            zNew = renderUtils.getFormula(t, loc1.z, loc2.z, control1.z, control2.z)

            xDif = xNew - xOld
            yDif = yNew - yOld
            zDif = zNew - zOld

            yaw = atan2(zDif, xDif)
            pitch = atan2(yDif, sqrt(zDif.pow(2.0) + xDif.pow(2.0)))
            roll = if (abs(rollDif) != 360.0) { rollStart + rollDif * t }
                   else                       { rollStart               }

            pitch = pitch / PI * -180
            yaw = yaw / PI * 180


            pitch = matchPrevious(pitch, prev.pitch, endRideNode.pitch)
            yaw = matchPrevious(yaw, prev.yaw, endRideNode.yaw)

            val render = RideNode(xNew, yNew, zNew, yaw, pitch, roll)

           // if (abs(yaw - prev.yaw) > 50)
            //    render = RideNode(xNew, yNew, zNew, prev.yaw, pitch, roll)

            renderTrackPiece(player, render)
            prev = render

            xOld = xNew
            yOld = yNew
            zOld = zNew
            t += 1 / density
            i++

        }

        Bukkit.broadcastMessage("Rend: ${prev.pitch} ${prev.yaw}")
        Bukkit.broadcastMessage("Rend: ${endRideNode.pitch} ${endRideNode.yaw}")

    }

    private fun matchPrevious(new: Double, old: Double, end: Double): Double {

        var newVal = new
        var count = 0;

        while (old - newVal > 15) {
            newVal += 180
            count++
            if (count > 100)
                break
        }
        count = 0

        while (old - newVal < -15) {
            newVal -= 180
            count++
            if (count > 100)
                break
        }

        return newVal;

    }

    private fun renderTrackPiece(player: Player, node: RideNode) {

        val loc = Location(player.world, node.locX, node.locY + 0.5, node.locZ)
        val renderUtils = RenderUtils()
        val angle: EulerAngle = renderUtils.getCorrectArmorStandAngle(node.yaw, node.pitch, node.roll)!!
        val stand = standCreator.create(location = loc, name = "", visible = false, small = true, head = angle, helmet = ItemStack(Material.WHITE_CONCRETE))

        editingPlayers[player]!!.editorEntities.add(stand)

    }

    private fun renderNode(editorData: EditorData, player: Player, node: EditorNode, id: Int) {

        val editingPlayer: EditingPlayer = editingPlayers[player]!!

        val loc = Location(player.world, node.locX, node.locY, node.locZ)
        val renderUtils = RenderUtils()
        val angle: EulerAngle = renderUtils.getCorrectArmorStandAngle(node.yaw, node.pitch, node.roll * -1)!!
        val name = if (editorData.selectedNode == id) "§a§l>>[$id§a§l]<<" else "§7[$id]"
        val helmet = if (editorData.selectedNode == id) ItemStack(Material.LIME_CONCRETE) else ItemStack(Material.GRAY_CONCRETE)
        val stand = standCreator.create(location = loc, name = name, visible = false, nameVisible = true, head = angle, helmet = helmet)

        editingPlayer.editorEntities.add(stand)

        if (editorData.selectedNode != id) return

        var height = 0.25

        for (connection in node.connections) {
            renderNodeConnection(player, node, connection, height)
            height += 0.25
        }

    }

    private fun renderNodeConnection(player: Player, node: EditorNode, id: Int, height: Double) {

        val editingPlayer: EditingPlayer = editingPlayers[player]!!

        val loc = Location(player.world, node.locX, node.locY + height, node.locZ)
        val stand = standCreator.create(location = loc, name = "§7$id", visible = false, nameVisible = true)

        editingPlayer.editorEntities.add(stand)

    }

    /*
    Player inventory management
     */

    private fun changeItems(player: Player) {

        if (player.isEditing()) {

            val inv = player.inventory
            val items = arrayOfNulls<ItemStack>(4)
            val itemUtil = main.itemUtils

            if (inv.getItem(2)!!.type == Material.RED_CONCRETE) {
                items[0] = itemUtil.createItem(Material.CLOCK, "§7Yaw")
                items[1] = itemUtil.createItem(Material.GRINDSTONE, "§7Pitch")
                items[2] = itemUtil.createItem(Material.STICK, "§7Roll")
                items[3] = itemUtil.createItem(Material.OAK_FENCE, "§7Radius")
            } else {
                items[0] = itemUtil.createItem(Material.RED_CONCRETE, "§7X")
                items[1] = itemUtil.createItem(Material.LIME_CONCRETE, "§7Y")
                items[2] = itemUtil.createItem(Material.BLUE_CONCRETE, "§7Z")
                items[3] = itemUtil.createItem(Material.CHAIN, "§7Invert")
            }

            for (i in 0..3) {
                inv.setItem(i + 2, items[i])
            }

            player.sendInfo("Changed editor items")

        }

    }

    private fun switchInventory(player: Player, isEditing: Boolean) {

        if (isEditing) {

            val inventory = player.inventory

            val newItems = mutableListOf<ItemStack?>()
            val oldItems = mutableListOf<ItemStack?>()
            val itemUtil = main.itemUtils

            newItems.add(itemUtil.createItem(Material.EMERALD, "§7Add Point"))
            newItems.add(itemUtil.createItem(Material.REDSTONE, "§7Rem Point"))
            newItems.add(itemUtil.createItem(Material.CLOCK, "§7Yaw"))
            newItems.add(itemUtil.createItem(Material.GRINDSTONE, "§7Pitch"))
            newItems.add(itemUtil.createItem(Material.STICK, "§7Roll"))
            newItems.add(itemUtil.createItem(Material.OAK_FENCE, "§7Radius"))
            newItems.add(ItemStack(Material.AIR))
            newItems.add(itemUtil.createItem(Material.BOOK, "§7Select Point"))
            newItems.add(itemUtil.createItem(Material.TRIPWIRE_HOOK, "§7Connect Point"))

            for (i in 0..8) {
                oldItems.add(inventory.getItem(i))
                inventory.setItem(i, newItems[i])
            }

            editingPlayers[player]?.playerInventory = oldItems

        } else {
            val inventory = player.inventory
            val editingPlayer = editingPlayers[player]
            val restore = editingPlayer?.playerInventory

            if (restore != null) {

                for (i in 0..8) { inventory.setItem(i, restore[i]) }
            } else {
                player.sendError("Inventory contents empty")
            }

        }

    }

    /*
    Player extensions
     */

    private fun Player.isEditing(): Boolean {
        return editingPlayers.containsKey(this)
    }

    private fun Player.sendInfo(info: String) {
        this.sendMessage("§6§lMCW-Rides §8► §7$info§7.")
    }

    private fun Player.sendError(error: String) {
        this.sendMessage("§6§lMCW-Rides §8► §c$error§c.")
    }

}