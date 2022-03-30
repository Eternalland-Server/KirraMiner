@file:Suppress("SpellCheckingInspection")

package net.sakuragame.eternal.kirraminer.ore

enum class OreState {
    IDLE, DIGGING, COOLDOWN, FINAL;

    fun getString(ore: Ore): String {
        return when (this) {
            IDLE -> ore.digMetadata.digEntityName.idle
            DIGGING -> ore.digMetadata.digEntityName.idle
            COOLDOWN -> ore.digMetadata.digEntityName.after
            FINAL -> ore.digMetadata.digEntityName.idle
        }
    }
}