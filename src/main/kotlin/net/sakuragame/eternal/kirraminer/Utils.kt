package net.sakuragame.eternal.kirraminer

import ink.ptms.zaphkiel.ZaphkielAPI
import ink.ptms.zaphkiel.api.ItemStream
import ink.ptms.zaphkiel.taboolib.module.nms.ItemTagData
import net.minecraft.server.v1_12_R1.PacketPlayOutAnimation
import net.minecraft.server.v1_12_R1.PacketPlayOutCollect
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.sendPacket
import taboolib.platform.util.giveItem
import taboolib.platform.util.isAir
import kotlin.math.roundToInt

fun Block.remove() {
    type = Material.AIR
}

fun Location.parseToString(): String {
    return "${world.name}@$x@$y@$z@$yaw@$pitch"
}

fun String.parseToLoc(): Location? {
    val split = split("@")
    if (split.size != 6) {
        return null
    }
    val world = Bukkit.getWorld(split[0]) ?: return null
    val x = split[1].toDoubleOrNull() ?: return null
    val y = split[2].toDoubleOrNull() ?: return null
    val z = split[3].toDoubleOrNull() ?: return null
    val yaw = split[4].toFloatOrNull() ?: return null
    val pitch = split[5].toFloatOrNull() ?: return null
    return Location(world, x, y, z, yaw, pitch)
}

fun ItemStack?.getStream(): ItemStream? {
    if (this == null || isAir()) {
        return null
    }
    val itemStream = ZaphkielAPI.read(this)
    if (itemStream.isVanilla()) {
        return null
    }
    return itemStream
}

@Suppress("SpellCheckingInspection")
fun ItemStack?.getZaphkielName(): String? {
    val itemStream = getStream() ?: return null
    return itemStream.getZaphkielName()
}

fun getPickaxeLevel(player: Player): Int? {
    return getPickaxeLevel(player.inventory.itemInMainHand)
}

fun getPickaxeLevel(item: ItemStack?): Int? {
    val name = item.getZaphkielName() ?: return null
    val pickaxeLevel = KirraMiner.conf.getInt("settings.pickaxe.$name.level")
    if (pickaxeLevel == 0) {
        return null
    }
    return pickaxeLevel
}

fun getPickaxeDurability(item: ItemStack?): Int? {
    val itemStream = item.getStream() ?: return null
    return itemStream.getZaphkielData().getDeep("pickaxe.durability")?.asInt() ?: return null
}

fun setPickaxeDurability(player: Player, item: ItemStack?, durability: Int): ItemStack? {
    val itemStream = item.getStream() ?: return null
    itemStream.getZaphkielData().putDeep("pickaxe.durability", ItemTagData(durability))
    return itemStream.rebuildToItemStack(player)
}

fun getPickaxeMaxDurability(item: ItemStack?): Int? {
    val name = item.getZaphkielName() ?: return null
    return KirraMiner.conf.getInt("settings.pickaxe.$name.max-durability")
}

fun getPickaxeMaxDurability(name: String): Int? {
    return KirraMiner.conf.getInt("settings.pickaxe.$name.max-durability")
}

fun getPickaxeDurabilityOnItem(durability: Int, item: ItemStack): Int? {
    val itemMaxDurability = item.type.maxDurability.toDouble()
    val maxDurability = getPickaxeMaxDurability(item)?.toDouble() ?: return null
    val percent = durability / maxDurability
    return (itemMaxDurability - (itemMaxDurability * percent)).roundToInt()
}

@Suppress("SpellCheckingInspection")
fun printToConsole(message: String) = Bukkit.getConsoleSender().sendMessage("[KirraMiner] $message")

fun String.splitIgnoreAllSpaces(symbol: String) = replace(" ", "").split(symbol)

fun Player.collectItem(item: Item) {
    sendPacket(PacketPlayOutCollect(item.entityId, entityId, item.itemStack.amount))
    Bukkit.getPluginManager().callEvent(EntityPickupItemEvent(this, item, item.itemStack.amount))
    giveItem(item.itemStack)
}

fun Player.swingHand() {
    sendPacket(PacketPlayOutAnimation((player as CraftPlayer).handle, 0))
}