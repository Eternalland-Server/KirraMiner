package net.sakuragame.eternal.kirraminer.ore.sub

import net.sakuragame.eternal.kirraminer.splitIgnoreAllSpaces

data class IntInterval(val min: Int, val max: Int) {

    val random: Int
        get() = (min..max).random()

    companion object {

        fun fromString(str: String): IntInterval? {
            if (str == "-1") {
                return IntInterval(-1, -1)
            }
            val split = str.splitIgnoreAllSpaces("-")
            if (split.size != 2) return null
            return IntInterval(split[0].toIntOrNull() ?: return null, split[1].toIntOrNull() ?: return null)
        }
    }

    override fun toString() = "$min-$max"
}