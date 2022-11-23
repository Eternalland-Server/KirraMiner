package net.sakuragame.eternal.kirraminer.function

import ink.ptms.zaphkiel.api.event.ItemBuildEvent
import ink.ptms.zaphkiel.api.event.ItemReleaseEvent
import net.sakuragame.eternal.kirraminer.KirraMiner
import net.sakuragame.eternal.kirraminer.KirraMinerAPI
import net.sakuragame.eternal.kirraminer.getPickaxeDurabilityOnItem
import net.sakuragame.eternal.kirraminer.getPickaxeMaxDurability
import org.bukkit.event.world.WorldUnloadEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

object FunctionListener {

    private val durabilityLore by lazy {
        KirraMiner.conf.getStringList("settings.durability-lore")
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun e(e: WorldUnloadEvent) {
        KirraMinerAPI.removeAllOresInWorld(e.world)
    }

    @SubscribeEvent
    fun e(e: ItemBuildEvent.Pre) {
        val toReplaced = mutableListOf<String>()
        val durability = e.itemStream.getZaphkielData().getDeep("pickaxe.durability")?.asInt() ?: return
        val maxDurability = getPickaxeMaxDurability(e.itemStream.getZaphkielName())
        durabilityLore.forEach {
            toReplaced += it
                .replace("<durability>", durability.toString())
                .replace("<max-durability>", maxDurability.toString())
        }
        e.addLore("DURABILITY", toReplaced)
    }

    @SubscribeEvent
    fun e(e: ItemReleaseEvent.Final) {
        val item = e.itemStack
        val pickaxeDurability = getPickaxeDurabilityOnItem(item) ?: return
        e.itemStack = e.itemStack.apply {
            durability = pickaxeDurability
        }
    }
}