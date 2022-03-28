package net.sakuragame.eternal.kirraminer.function

import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.getPickaxeLevel
import net.sakuragame.eternal.kirraminer.sendActionMessage
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.Baffle
import taboolib.platform.util.asLangText
import java.util.concurrent.TimeUnit

object FunctionDigListener {

    private val baffle by lazy {
        Baffle.of(200, TimeUnit.MILLISECONDS)
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEntityEvent) {
        if (e.hand != EquipmentSlot.HAND) {
            return
        }
        val player = e.player
        val ore = KirraMinerAPI.getOreByEntityUUID(e.rightClicked.uniqueId) ?: return
        if (!baffle.hasNext(player.name)) {
            player.sendMessage(player.asLangText("message-player-baffle"))
            return
        }
        baffle.next(player.name)
        if (ore.digState.isRefreshing) {
            return
        }
        val pickaxeLevel = getPickaxeLevel(player) ?: return
        if (ore.digMetadata.digLevel > pickaxeLevel) {
            player.sendActionMessage(player.asLangText("message-player-pickaxe-level-insufficient"))
            return
        }
        ore.dig(player)
    }
}