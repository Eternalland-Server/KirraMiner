package net.sakuragame.eternal.kirraminer.function

import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import org.bukkit.event.world.WorldUnloadEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object FunctionListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: WorldUnloadEvent) {
        KirraMinerAPI.removeOresOfWorld(e.world)
    }
}