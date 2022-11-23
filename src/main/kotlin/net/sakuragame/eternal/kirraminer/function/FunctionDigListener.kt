package net.sakuragame.eternal.kirraminer.function

import net.sakuragame.eternal.kirraminer.KirraMiner
import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.getZaphkielName
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

@Suppress("DuplicatedCode")
object FunctionDigListener {

    private val pickaxes by lazy {
        KirraMiner.conf.getConfigurationSection("settings.pickaxe")!!.getKeys(false)
    }

    @SubscribeEvent
    fun e(e: PlayerItemHeldEvent) {
        val player = e.player
        if (player.gameMode == GameMode.CREATIVE) {
            return
        }
        val item = player.inventory.getItem(e.newSlot)
        val pickaxeHeld = pickaxes.contains(item.getZaphkielName())
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
        e.isCancelled = true
        e.block.type = Material.AIR
        ore.afterDig(player, player.inventory.itemInMainHand, null)
    }

    @SubscribeEvent(EventPriority.LOWEST, ignoreCancelled = true)
    fun e1(e: BlockBreakEvent) {
        val player = e.player
        val block = e.block
        if (KirraMinerAPI.getOreByLocation(block.location) != null) {
            return
        }
        val ore = FunctionDigSimulation.getOreFromBlockData(block) ?: return
        e.isCancelled = true
        e.block.type = Material.AIR
        ore.afterDig(player, player.inventory.itemInMainHand, block.location, refresh = false)
    }
}