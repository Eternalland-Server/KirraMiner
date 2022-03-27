package net.sakuragame.eternal.kirraminer.ore

import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import net.sakuragame.eternal.kirraminer.printToConsole
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit
import java.util.UUID

data class Ore(val id: String, val loc: Location, val refreshTime: IntInterval, val digMetadata: DigMetadata, val digState: DigState) {

    fun init() {
        digState.entity = KirraMinerAPI.generateOreEntity(digMetadata.name, loc)

    }

    fun dig(player: Player) {

    }

    fun refresh() {
        printToConsole("开始刷新矿物: $id...")
        digState.isRefreshing = false
        digState.futureRefreshMillis = System.currentTimeMillis()
        init()
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

        fun getOreByEntityUUID(uuid: UUID) = KirraMinerAPI.ores.values.firstOrNull { uuid.equals(it.digMetadata.name) }
    }
}