package net.sakuragame.eternal.kirraminer

import ink.ptms.zaphkiel.ZaphkielAPI
import net.minecraft.server.v1_12_R1.PacketPlayOutAnimation
import net.minecraft.server.v1_12_R1.PacketPlayOutCollect
import net.sakuragame.eternal.justmessage.api.MessageAPI
import org.apache.commons.lang3.time.DateFormatUtils
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import taboolib.module.nms.sendPacket
import taboolib.platform.util.giveItem
import taboolib.platform.util.isAir

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
    sendPacket(PacketPlayOutCollect(item.entityId, player.entityId, item.itemStack.amount))
    player.giveItem(item.itemStack)
}

fun Player.swingHand() {
    sendPacket(PacketPlayOutAnimation((player as CraftPlayer).handle, 0))
}

// copied from random project, it works, and don't touch it.
fun <T : Entity> getTargetedEntity(player: Player, entities: List<T>): T? {
    var target: T? = null
    val threshold = 1.0
    for (other in entities) {
        val n = other.location.toVector().subtract(player.location.toVector())
        if (player.location.direction.normalize().crossProduct(n).lengthSquared() < threshold && n.normalize().dot(player.location.direction.normalize()) >= 0) {
            if (target == null || target.location.distanceSquared(player.location) > other.location.distanceSquared(player.location)) {
                if (target is Player) {
                    if (target.gameMode == GameMode.SPECTATOR) continue
                }
            }
            target = other
        }
    }
    return target
}