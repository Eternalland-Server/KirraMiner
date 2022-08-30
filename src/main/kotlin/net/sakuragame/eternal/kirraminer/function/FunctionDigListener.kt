package net.sakuragame.eternal.kirraminer.function

import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.getPickaxeLevel
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import taboolib.common.platform.event.SubscribeEvent

object FunctionDigListener {

    @SubscribeEvent
    fun e(e: PlayerItemHeldEvent) {
        val player = e.player
        if (player.gameMode == GameMode.CREATIVE) {
            return
        }
        val item = player.inventory.getItem(e.newSlot)
        val pickaxeHeld = getPickaxeLevel(item) != null
        when {
            player.gameMode == GameMode.ADVENTURE && pickaxeHeld -> player.gameMode = GameMode.SURVIVAL
            player.gameMode == GameMode.SURVIVAL && !pickaxeHeld -> player.gameMode = GameMode.ADVENTURE
            else -> return
        }
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
        ore.afterDig(player, player.inventory.itemInMainHand)
    }
}