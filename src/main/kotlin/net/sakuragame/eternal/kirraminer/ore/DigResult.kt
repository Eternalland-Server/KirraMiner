package net.sakuragame.eternal.kirraminer.ore

import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import net.sakuragame.eternal.kirraminer.splitIgnoreAllSpaces

data class DigResult(val itemId: String, val amount: IntInterval) {

    companion object {

        fun fromString(str: String): DigResult? {
            val split = str.splitIgnoreAllSpaces(",")
            if (split.size != 2) return null
            return DigResult(split[0], IntInterval.fromString(split[1]) ?: return null)
        }
    }

    override fun toString() = "$itemId, $amount"
}