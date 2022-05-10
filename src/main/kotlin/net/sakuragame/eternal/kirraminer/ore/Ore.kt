package net.sakuragame.eternal.kirraminer.ore

import net.sakuragame.eternal.kirraminer.*
import net.sakuragame.eternal.kirraminer.event.MineEndEvent
import net.sakuragame.eternal.kirraminer.event.MineStartEvent
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import taboolib.platform.util.asLangText

data class Ore(val id: String, val loc: Location, val refreshTime: IntInterval, val digMetadata: DigMetadata, val digState: DigState) {

    fun dig(player: Player, profile: Profile) {
        MineStartEvent(player, this).call()
        val maxDigTime = digMetadata.digTime
        digState.isDigging = true
        profile.startDigging(this)
        submit(async = true, delay = 3L) {
            KirraMinerAPI.generateHologram(this@Ore, OreState.DIGGING, profile)
        }
        submit(async = true, delay = 5L, period = 10L) {
            if (!player.isOnline) {
                cancel()
                return@submit
            }
            val targetedEntity = player.getLookingEntity(3.0)
            if (targetedEntity == null || targetedEntity.type != EntityType.ARMOR_STAND || targetedEntity.location.distance(player.location) > 4) {
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
        digState.isDigging = false
        digState.futureRefreshMillis = System.currentTimeMillis() + (refreshTime.random * 1000)
        digState.isRefreshing = true
        profile.reset()
    }

    private fun giveResult(player: Player) {
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

    private fun refreshHologram(profile: Profile) {
        val hologram = KirraMinerAPI.hologramMap[id] ?: return
        hologram.getPage(0).getLine(2).text = "&8( ${profile.getDiggingProgressBar() ?: ""} &8)".colored()
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
        if (digState.entity != null) {
            digState.entity?.remove()
            digState.entity = null
        }
        submit(async = false) {
            digState.entity = KirraMinerAPI.generateOreEntity(this@Ore, state)
        }
    }

    companion object {

        @Awake(LifeCycle.ACTIVE)
        fun i() {
            submit(async = true, period = 20L) {
                KirraMinerAPI.ores.values.filter { it.digState.isRefreshing }.filter { System.currentTimeMillis() >= it.digState.futureRefreshMillis }.forEach {
                    submit(async = true) {
                        it.refresh()
                    }
                }
            }
        }
    }
}