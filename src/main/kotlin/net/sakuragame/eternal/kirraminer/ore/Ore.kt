package net.sakuragame.eternal.kirraminer.ore

import net.sakuragame.eternal.kirraminer.*
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit
import taboolib.common5.util.createBar
import taboolib.module.chat.colored
import taboolib.platform.util.asLangText

data class Ore(val id: String, val loc: Location, val refreshTime: IntInterval, val digMetadata: DigMetadata, val digState: DigState) {

    fun dig(player: Player) {
        digState.isDigging = true
        val digTime = digMetadata.digTime
        var currentDigTime = 0.0
        submit(async = true, period = 10L) {
            if (!player.isOnline) {
                cancel()
                return@submit
            }
            val targetedEntity = getTargetedEntity(player, player.world.entities)
            if (targetedEntity == null) {
                if (currentDigTime > 1) {
                    player.playSound(player.location, Sound.BLOCK_NOTE_BASS, 1f, 1.5f)
                    player.sendTitle("", "&c&l结束挖矿! &4&l✘".colored(), 0, 30, 0)
                    player.sendActionMessage(player.asLangText("message-player-not-target-ore"))
                }
                digState.isDigging = false
                cancel()
                return@submit
            }
            currentDigTime += 0.5
            player.swingHand()
            player.sendTitle("", getProgressBar(currentDigTime, digTime), 0, 40, 0)
            player.playSound(player.location, Sound.BLOCK_STONE_STEP, 1f, 1.5f)
            if (currentDigTime >= digTime) {
                doAfterDig(player)
                cancel()
                return@submit
            }
        }
    }

    fun refresh() {
        printToConsole("开始刷新矿物: $id...")
        digState.isRefreshing = false
        digState.futureRefreshMillis = System.currentTimeMillis()
        init()
        printToConsole("刷新完毕.")
    }

    private fun doAfterDig(player: Player) {
        player.sendTitle("", "&f&l挖掘完毕. &a&l✓".colored(), 0, 15, 0)
        submit(delay = 2L) {
            player.playSound(player.location, Sound.BLOCK_ANVIL_BREAK, 1f, 1.5f)
        }
        submit(delay = 5L) {
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)
        }
        giveResult(player)
        submit(async = false) {
            init(after = true)
        }
        digState.futureRefreshMillis = System.currentTimeMillis() + (refreshTime.random * 1000)
        digState.isRefreshing = true
    }

    private fun giveResult(player: Player) {
        submit(async = false) {
            val itemStack = digMetadata.digResult.getResultItem(player) ?: return@submit
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

    private fun getProgressBar(current: Double, max: Int): String {
        val str = createBar("&7|", "&a|", 20, current / max.toDouble())
        return "&6&l挖掘进度: &8( $str &8)".colored()
    }

    private fun init(after: Boolean = false) {
        val name = when (after) {
            true -> digMetadata.digEntityName.after
            false -> digMetadata.digEntityName.idle
        }
        val currentEntity = digState.entity
        if (currentEntity != null) {
            currentEntity.remove()
            digState.entity = null
        }
        digState.entity = KirraMinerAPI.generateOreEntity(name, loc)
    }

    companion object {

        @Awake(LifeCycle.ACTIVE)
        fun i() {
            submit(async = true, period = 20L) {
                KirraMinerAPI.ores.values
                    .filter { it.digState.isRefreshing }
                    .filter { System.currentTimeMillis() >= it.digState.futureRefreshMillis }
                    .forEach {
                        it.refresh()
                    }
            }
        }
    }
}