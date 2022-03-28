package net.sakuragame.eternal.kirraminer.ore

import net.sakuragame.eternal.kirraminer.*
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit
import taboolib.common5.util.createBar
import taboolib.module.chat.colored

data class Ore(val id: String, val loc: Location, val refreshTime: IntInterval, val digMetadata: DigMetadata, val digState: DigState) {

    fun dig(player: Player) {
        val digTime = digMetadata.digTime
        var currentDigTime = 0
        submit(async = true, period = 20L) {
            if (!player.isOnline) {
                cancel()
                return@submit
            }
            val targetedEntity = getTargetedEntity(player, player.world.entities)
            if (targetedEntity == null) {
                player.sendActionMessage("message-player-not-target-ore")
                cancel()
                return@submit
            }
            player.swingHand()
            currentDigTime += 1
            player.sendTitle("", getProgressBar(currentDigTime, digTime), 0, 40, 0)
            if (currentDigTime >= digTime) {
                giveResult(player)
                init(after = true)
                digState.futureRefreshMillis = System.currentTimeMillis() + digTime * 1000
                digState.isRefreshing = true
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

    private fun giveResult(player: Player) {

    }

    private fun getProgressBar(current: Int, max: Int): String {
        val str = createBar("&7|", "&a|", 20, (current / max) * 0.01)
        return "&6&l挖掘进度: &8[ $str &8]".colored()
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