package net.sakuragame.eternal.kirraminer.ore.sub

import net.sakuragame.eternal.kirraminer.splitIgnoreAllSpaces

data class IntInterval(val min: Int, val max: Int) {

    companion object {

        fun fromString(str: String): IntInterval? {
            val split = str.splitIgnoreAllSpaces("-")
            if (split.size != 2) return null
            return IntInterval(split[0].toIntOrNull() ?: return null, split[1].toIntOrNull() ?: return null)
        }
    }

    fun getRandom() = (min..max).random()

    override fun toString() = "$min-$max"
}