package net.sakuragame.eternal.kirraminer.function

import net.sakuragame.eternal.kirraminer.ore.OreLocation
import org.bukkit.Location

object FunctionOreCreate {

    private var locA: Location? = null
    private var locB: Location? = null
    private var yLimit: Int? = null

    fun setLocA(loc: Location) {
        locA = loc
    }

    fun setLocB(loc: Location) {
        locB = loc
    }

    fun setYLimit(y: Int) {
        yLimit = y
    }

    fun getLoc(): OreLocation? {
        if (locA == null || locB == null || yLimit == null) {
            return null
        }
        return OreLocation(locA!!, locB!!, yLimit!!)
    }
}