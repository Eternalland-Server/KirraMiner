package net.sakuragame.eternal.kirraminer

import org.bukkit.Location

data class Ore(val id: String, val loc: Location, val refreshTime: IntInterval, val digMetadata: List<DigMetadata>, val digState: DigState) {

    data class IntInterval(val min: Int, val max: Int) {

        companion object {

            fun fromString(str: String): IntInterval? {
                val split = str.replaceAllSpace().split("-")
                if (split.size != 2) return null
                return IntInterval(split[0].toIntOrNull() ?: return null, split[1].toIntOrNull() ?: return null)
            }
        }

        fun getRandom() = (min..max).random()

        override fun toString() = "$min-$max"
    }

    data class DigMetadata(val weight: Int, val digLevel: Int, val digTime: Int, val digResult: DigResult) {

        data class DigResult(val itemId: String, val amount: IntInterval) {

            companion object {

                fun fromString(str: String): DigResult? {
                    val split = str.replaceAllSpace().split(",")
                    if (split.size != 2) return null
                    return DigResult(split[0], IntInterval.fromString(split[1]) ?: return null)
                }
            }

            override fun toString() = "$itemId, $amount"
        }
    }

    data class DigState(var isDigging: Boolean, var isRefreshing: Boolean)
}