package net.sakuragame.eternal.kirraminer.function

import ink.ptms.zaphkiel.api.event.ItemBuildEvent
import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.getPickaxeMaxDurability
import org.bukkit.event.world.WorldUnloadEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored

object FunctionListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: WorldUnloadEvent) {
        KirraMinerAPI.removeAllOresInWorld(e.world)
    }

    @SubscribeEvent
    fun e(e: ItemBuildEvent.Pre) {
        val durability = e.itemStream.getZaphkielData().getDeep("pickaxe.durability")?.asInt() ?: return
        val maxDurability = getPickaxeMaxDurability(e.itemStream.getZaphkielName())
        e.addLore("DURABILITY", listOf("", "&e耐久度: &f$durability / $maxDurability".colored(), ""))
    }
}