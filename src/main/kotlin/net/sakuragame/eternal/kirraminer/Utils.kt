package net.sakuragame.eternal.kirraminer

import ink.ptms.zaphkiel.ZaphkielAPI
import net.minecraft.server.v1_12_R1.PacketPlayOutAnimation
import net.minecraft.server.v1_12_R1.PacketPlayOutCollect
import net.sakuragame.eternal.justmessage.api.MessageAPI
import org.apache.commons.lang3.time.DateFormatUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import taboolib.module.nms.sendPacket
import taboolib.platform.util.giveItem
import taboolib.platform.util.isAir

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

fun getTextureDate(date: Long): String {
    return DateFormatUtils.format(date, "HH:mm:ss")
}

fun getPickaxeLevel(player: Player): Int? {
    val item = player.inventory.itemInMainHand
    if (item.isAir()) {
        return null
    }
    val itemStream = ZaphkielAPI.read(item)
    if (itemStream.isVanilla()) {
        return null
    }
    return itemStream.getZaphkielData().getDeep("pickaxe.level")?.asInt() ?: return null
}

@Suppress("SpellCheckingInspection")
fun printToConsole(message: String) = Bukkit.getConsoleSender().sendMessage("[KirraMiner] $message")

fun String.splitIgnoreAllSpaces(symbol: String) = replace(" ", "").split(symbol)

fun Player.sendActionMessage(str: String) = MessageAPI.sendActionTip(player, str)

fun Player.collectItem(item: Item) {
    sendPacket(PacketPlayOutCollect(item.entityId, entityId, item.itemStack.amount))
    Bukkit.getPluginManager().callEvent(EntityPickupItemEvent(this, item, item.itemStack.amount))
    giveItem(item.itemStack)
}

fun Player.swingHand() {
    sendPacket(PacketPlayOutAnimation((player as CraftPlayer).handle, 0))
}

fun LivingEntity.getLookingEntity(radius: Double): Entity? {
    val entities = getNearbyEntities(radius, radius, radius)
    entities.forEach {
        val direct = it.location.clone().subtract(location).toVector().setY(0).normalize()
        val lookDir = this.eyeLocation.direction.setY(0).normalize()
        val dot = direct.dot(lookDir)
        if (dot > 0.9) {
            return it
        }
    }
    return null
}