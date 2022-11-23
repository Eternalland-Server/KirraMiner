package net.sakuragame.eternal.kirraminer.ore

import ink.ptms.zaphkiel.ZaphkielAPI
import net.sakuragame.eternal.kirraminer.KirraMiner
import net.sakuragame.eternal.kirraminer.getIntRange
import net.sakuragame.eternal.kirraminer.getZaphkielName
import org.bukkit.inventory.ItemStack

data class DigMetadata(val id: String, val weight: Int, val name: String, val oreIndex: String, val dropItem: String, val costDurability: IntRange, val provideExp: IntRange) {

    fun getResultItem(pickaxe: ItemStack): ItemStack? {
        val name = pickaxe.getZaphkielName() ?: return null
        val amountRange = KirraMiner.conf.getIntRange("settings.pickaxe.$name.dig") ?: return null
        val item = ZaphkielAPI.getItem(dropItem) ?: return null
        return item.rebuildToItemStack().apply {
            amount = amountRange.random()
        }
    }
}