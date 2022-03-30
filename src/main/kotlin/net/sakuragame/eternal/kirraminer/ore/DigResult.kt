package net.sakuragame.eternal.kirraminer.ore

import ink.ptms.zaphkiel.ZaphkielAPI
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import net.sakuragame.eternal.kirraminer.splitIgnoreAllSpaces
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class DigResult(val itemId: String, val amount: IntInterval) {

    companion object {

        fun fromString(str: String): DigResult? {
            val split = str.splitIgnoreAllSpaces(",")
            if (split.size != 2) return null
            return DigResult(split[0], IntInterval.fromString(split[1]) ?: return null)
        }
    }

    fun getResultItem(player: Player?): ItemStack? {
        val itemStream = ZaphkielAPI.getItem(itemId) ?: return null
        val itemStack = itemStream.rebuildToItemStack(player).also {
            it.amount = amount.random
        }
        return itemStack
    }

    override fun toString() = "$itemId, $amount"
}