package com.mcw.rides.generic.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ItemUtil {

    fun createItem(material: Material, name: String): ItemStack {

        val itemStack = ItemStack(material)
        val meta = itemStack.itemMeta
        meta?.setDisplayName(name)
        itemStack.itemMeta = meta
        return itemStack

    }

}