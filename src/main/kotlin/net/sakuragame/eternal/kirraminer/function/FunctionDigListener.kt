package net.sakuragame.eternal.kirraminer.function

import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.getPickaxeLevel
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import taboolib.common.platform.event.SubscribeEvent

object FunctionDigListener {

    @SubscribeEvent
    fun e(e: PlayerItemHeldEvent) {
        Bukkit.broadcastMessage("reached 1")
    }

    @SubscribeEvent
    fun e(e: BlockBreakEvent) {
        val player = e.player
        val block = e.block
        val ore = KirraMinerAPI.getOreByLocation(block.location) ?: return
        val pickaxeLevel = getPickaxeLevel(player) ?: return
        if (ore.digMetadata.digLevel > pickaxeLevel) {
            return
        }
        e.isCancelled = true
        e.block.type = Material.AIR
        ore.afterDig(player)
    }
}