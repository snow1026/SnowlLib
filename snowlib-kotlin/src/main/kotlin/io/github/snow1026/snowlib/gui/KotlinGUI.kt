package io.github.snow1026.snowlib.gui

import io.github.snow1026.snowlib.gui.events.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun gui(rows: Int, title: String, init: GUIDSL.() -> Unit): GUI {
    val gui = GUI.create(rows, title)
    GUIDSL(gui).apply(init)
    return gui
}

class GUIDSL(val gui: GUI) {

    fun slot(vararg indexes: Int, init: GUISlotDSL.() -> Unit) {
        val slot = gui.slot(*indexes)
        GUISlotDSL(slot).apply(init)
    }

    fun slot(range: IntRange, init: GUISlotDSL.() -> Unit) {
        val slot = gui.slot(*range.toList().toIntArray())
        GUISlotDSL(slot).apply(init)
    }

    fun fill(itemStack: ItemStack) {
        gui.fill(itemStack)
    }

    fun fill(material: Material, block: (ItemStack.() -> Unit)? = null) {
        val item = ItemStack(material)
        block?.invoke(item)
        gui.fill(item)
    }

    fun onClick(handler: (GUIClickEvent) -> Unit) { gui.onClick(handler) }
    fun onOpen(handler: (GUIOpenEvent) -> Unit) { gui.onOpen(handler) }
    fun onClose(handler: (GUICloseEvent) -> Unit) { gui.onClose(handler) }
    fun onDrag(handler: (GUIDragEvent) -> Unit) { gui.onDrag(handler) }
    fun onInteract(handler: (GUIInteractEvent) -> Unit) { gui.onInteract(handler) }
    fun onMoveItem(handler: (GUIMoveItemEvent) -> Unit) { gui.onMoveItem(handler) }
}

class GUISlotDSL(val slot: GUISlot) {

    var item: ItemStack?
        get() = slot.item
        set(value) { slot.item(value) }


    fun item(material: Material, amount: Int = 1, builder: (ItemStack.() -> Unit)? = null) {
        val itemStack = ItemStack(material, amount)
        builder?.invoke(itemStack)
        slot.item(itemStack)
    }

    fun onClick(handler: (GUIClickEvent) -> Unit) {
        slot.onClick(handler)
    }
}

fun ItemStack.meta(block: ItemMeta.() -> Unit) {
    val m = itemMeta ?: return
    block(m)
    itemMeta = m
}