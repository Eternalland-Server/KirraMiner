package net.sakuragame.eternal.kirraminer

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

object Loader {

    @Awake(LifeCycle.ENABLE)
    fun i() {
        printToConsole("-- 正在加载矿物信息.")
        KirraMinerAPI.ores.clear()
        KirraMiner.oresFile.getKeys(false).forEach {
            val digMetadataList = mutableListOf<Ore.DigMetadata>()
            val loc = KirraMiner.oresFile.getString("$it.loc")?.parseToLoc() ?: return@forEach
            val refreshTime = Ore.IntInterval.fromString(KirraMiner.oresFile.getString("$it.refresh-time") ?: return@forEach) ?: return@forEach
            (KirraMiner.oresFile.getConfigurationSection("$it.metadata-list") ?: return@forEach).getKeys(false).forEach metaForeach@{ metadataSection ->
                val weight = KirraMiner.oresFile.getInt("$it.metadata-list.$metadataSection.weight")
                val digLevel = KirraMiner.oresFile.getInt("$it.metadata-list.$metadataSection.dig-level")
                val digTime = KirraMiner.oresFile.getInt("$it.metadata-list.$metadataSection.dig-time")
                val digResult = Ore.DigMetadata.DigResult.fromString(KirraMiner.oresFile.getString("$it.metadata-list.$metadataSection.dig-result") ?: return@metaForeach) ?: return@metaForeach
                digMetadataList += Ore.DigMetadata(weight, digLevel, digTime, digResult)
            }
            KirraMinerAPI.ores += Ore(it, loc, refreshTime, digMetadataList, Ore.DigState(isDigging = false, isRefreshing = false))
        }
        printToConsole("-- 加载完毕, 一共加载了 ${KirraMinerAPI.ores.size} 个矿物.")
    }
}