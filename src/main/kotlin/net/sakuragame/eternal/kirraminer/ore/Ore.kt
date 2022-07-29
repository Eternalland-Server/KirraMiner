package net.sakuragame.eternal.kirraminer.ore

import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.collectItem
import net.sakuragame.eternal.kirraminer.event.MineEndEvent
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import net.sakuragame.eternal.kirraminer.printToConsole
import net.sakuragame.eternal.kirraminer.remove
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit

data class Ore(
    val id: String,
    val isTemp: Boolean = false,
    val loc: Location?,
    val refreshTime: IntInterval,
    val digState: DigState,
    var digMetadata: DigMetadata,
) {

    fun afterDig(player: Player) {
        doAfterDig(player)
        player.playSound(player.location, Sound.BLOCK_STONE_STEP, 1f, 1.5f)
    }

    fun refresh() {
        printToConsole("开始刷新矿物: $id...")
        digState.isRefreshing = false
        digState.futureRefreshMillis = System.currentTimeMillis()
        init()
        printToConsole("刷新完毕.")
    }

    private fun doAfterDig(player: Player) {
        submit(delay = 2L) {
            player.playSound(player.location, Sound.BLOCK_ANVIL_BREAK, 1f, 1.5f)
        }
        submit(delay = 5L) {
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)
        }
        giveResult(player)
        digState.futureRefreshMillis = System.currentTimeMillis() + (refreshTime.random * 1000)
        digState.isRefreshing = true
    }

    private fun giveResult(player: Player) {
        // should not happen.
        if (loc == null) {
            return
        }
        submit(async = false) {
            val itemStack = digMetadata.digResult.getResultItem(player) ?: return@submit
            MineEndEvent(player, this@Ore, itemStack).call()
            val droppedItem = loc.world.dropItem(loc.clone().add(0.0, 0.6, 0.0), itemStack).apply {
                pickupDelay = 999999
                isGlowing = true
            }
            submit(delay = 7L) {
                player.collectItem(droppedItem)
                droppedItem.remove()
            }
        }
    }

    private fun init() {
        digMetadata = KirraMinerAPI.getWeightRandomMetadataByID(id)!!
        if (digState.block != null) {
            digState.block?.remove()
            digState.block = null
        }
        submit(async = false) {
            digState.block = KirraMinerAPI.generateOreBlock(this@Ore)
        }
    }

    companion object {

        @Awake(LifeCycle.ACTIVE)
        fun i() {
            submit(async = true, period = 20L) {
                KirraMinerAPI.ores.values
                    .filter { it.digState.isRefreshing }
                    .filter { System.currentTimeMillis() >= it.digState.futureRefreshMillis }
                    .forEach {
                        submit(async = true) {
                            it.refresh()
                        }
                    }
            }
        }
    }
}