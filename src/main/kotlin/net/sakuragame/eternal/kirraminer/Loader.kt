package net.sakuragame.eternal.kirraminer

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

object Loader {

    @Awake(LifeCycle.ENABLE)
    fun i() {
        KirraMinerAPI.ores.clear()
        KirraMiner.oresFile.getKeys(false).forEach {
            val loc = KirraMiner.oresFile.getString("$it.loc")?.parseToLoc() ?: return@forEach
            val refreshTime = Ore.IntInterval.fromString(KirraMiner.oresFile.getString("$it.refresh-time") ?: return@forEach) ?: return@forEach
            val metadataSection = KirraMiner.oresFile.getConfigurationSection("$it.metadata-list") ?: return@forEach
            metadataSection.getKeys(false).forEach { metaSection ->

            }
        }
    }
}