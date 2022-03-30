package net.sakuragame.eternal.kirraminer

import net.sakuragame.eternal.kirraminer.ore.Ore
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.util.createBar

class Profile(val player: Player) {

    var isDigging = false

    var digTime = 0.0

    var diggingOreId: String? = null

    companion object {

        val profiles = mutableMapOf<String, Profile>()

        fun Player.profile() = profiles.values.firstOrNull { it.player.uniqueId == uniqueId }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        fun e(e: PlayerJoinEvent) {
            profiles[e.player.name] = Profile(e.player)
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        fun e(e: PlayerKickEvent) {
            dataRecycle(e.player)
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        fun e(e: PlayerQuitEvent) {
            dataRecycle(e.player)
        }

        private fun dataRecycle(player: Player) {
            player.profile()?.apply {
                drop()
            }
        }
    }

    fun drop() {
        profiles -= player.name
    }

    fun startDigging(ore: Ore) {
        isDigging = true
        diggingOreId = ore.id
    }

    fun reset() {
        digTime = 0.0
        diggingOreId = null
        isDigging = false
    }

    fun getDiggingProgressBar(): String? {
        if (!isDigging) {
            return null
        }
        val ore = KirraMinerAPI.ores[diggingOreId] ?: return null
        val max = ore.digMetadata.digTime
        return createBar("&7|", "&a|", 20, digTime / max.toDouble())
    }
}