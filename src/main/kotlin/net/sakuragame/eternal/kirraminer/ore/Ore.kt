package net.sakuragame.eternal.kirraminer.ore

import net.sakuragame.eternal.kirraminer.*
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.asLangText

data class Ore(val id: String, val loc: Location, val refreshTime: IntInterval, val digMetadata: DigMetadata, val digState: DigState) {

    fun dig(player: Player, profile: Profile) {
        val maxDigTime = digMetadata.digTime
        digState.isDigging = true
        profile.startDigging(this)
        submit(async = false, delay = 10L) {
            KirraMinerAPI.generateHologram(this@Ore, OreState.DIGGING, profile)
        }
        submit(async = true, period = 10L) {
            if (!player.isOnline) {
                cancel()
                return@submit
            }
            val targetedEntity = getTargetedEntity(player, player.world.entities)
            if (targetedEntity == null) {
                if (profile.digTime > 0.0) {
                    player.playSound(player.location, Sound.BLOCK_NOTE_BASS, 1f, 1.5f)
                    player.sendTitle("", "&c&l结束挖矿! &4&l✘".colored(), 0, 30, 0)
                    player.sendActionMessage(player.asLangText("message-player-not-target-ore"))
                }
                digState.isDigging = false
                KirraMinerAPI.generateHologram(this@Ore, OreState.IDLE, profile)
                profile.reset()
                cancel()
                return@submit
            }
            profile.digTime += 0.5
            player.swingHand()
            player.sendTitle("", getProgressBar(profile), 0, 40, 0)
            player.playSound(player.location, Sound.BLOCK_STONE_STEP, 1f, 1.5f)
            refreshHologram(profile)
            if (profile.digTime >= maxDigTime) {
                doAfterDig(player, profile)
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

    private fun doAfterDig(player: Player, profile: Profile) {
        player.sendTitle("", "&a&l挖掘完毕. &2&l✓".colored(), 0, 15, 0)
        submit(delay = 2L) {
            player.playSound(player.location, Sound.BLOCK_ANVIL_BREAK, 1f, 1.5f)
        }
        submit(delay = 5L) {
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)
        }
        giveResult(player)
        KirraMinerAPI.generateHologram(this, OreState.FINAL, profile)
        submit(async = false, delay = 10L) {
            init(after = true)
        }
        digState.futureRefreshMillis = System.currentTimeMillis() + (refreshTime.random * 1000)
        digState.isRefreshing = true
        profile.reset()
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

    private fun refreshHologram(profile: Profile) {
        val hologram = KirraMinerAPI.hologramMap[id] ?: return
        hologram.getTextLine(2).text = "&8( ${profile.getDiggingProgressBar() ?: ""} &8)".colored()
        Bukkit.getOnlinePlayers().forEach {
            hologram.refreshVisibility(it)
        }
    }

    private fun getProgressBar(profile: Profile): String {
        val bar = profile.getDiggingProgressBar()
        return "&6&l挖掘进度: &8( $bar &8)".colored()
    }

    private fun init(after: Boolean = false) {
        val state = when (after) {
            true -> OreState.COOLDOWN
            false -> OreState.IDLE
        }
        val currentEntity = digState.entity
        if (currentEntity != null) {
            currentEntity.remove()
            digState.entity = null
        }
        digState.entity = KirraMinerAPI.generateOreEntity(this, state)
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