package net.sakuragame.eternal.kirraminer.function

import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.getPickaxeLevel
import net.sakuragame.eternal.kirraminer.sendActionMessage
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import taboolib.platform.util.asLangText
import java.util.concurrent.TimeUnit

object FunctionDigListener {

    val baffle by lazy {
        Baffle.of(1, TimeUnit.SECONDS)
    }

    @SubscribeEvent
    fun e(e: PlayerInteractAtEntityEvent) {
        if (e.hand != EquipmentSlot.HAND) {
            return
        }
        val player = e.player
        val ore = KirraMinerAPI.getOreByEntityUUID(e.rightClicked.uniqueId) ?: return
        if (!baffle.hasNext(player.name)) {
            return
        }
        baffle.next(player.name)
        if (ore.digState.isRefreshing || ore.digState.isDigging) {
            return when {
                ore.digState.isRefreshing -> player.sendActionMessage(player.asLangText("message-player-ore-refreshing"))
                ore.digState.isDigging -> player.sendActionMessage(player.asLangText("message-player-ore-digging"))
                else -> return
            }
        }
        val pickaxeLevel = getPickaxeLevel(player) ?: return
        if (ore.digMetadata.digLevel > pickaxeLevel) {
            player.sendActionMessage(player.asLangText("message-player-pickaxe-level-insufficient"))
            return
        }
        ore.dig(player)
    }
}