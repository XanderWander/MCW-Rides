package com.mcw.rides.editor.objects.editor

import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack

data class EditingPlayer(var ride: String,
                         var playerInventory: MutableList<ItemStack?> = mutableListOf(),
                         var editorEntities: MutableList<ArmorStand> = mutableListOf()) {

    fun clearEntities() {
        for (stand in editorEntities) {
            stand.remove()
        }
        editorEntities = mutableListOf()
    }

}
