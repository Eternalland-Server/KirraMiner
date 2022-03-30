package net.sakuragame.eternal.kirraminer.function

import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.event.SubscribeEvent

object FunctionHologram {

    @SubscribeEvent
    fun e(e: PlayerJoinEvent) {
        val player = e.player
        KirraMinerAPI.hologramMap.values.forEach {
            it.refreshVisibility(player)
        }
    }
}