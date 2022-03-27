package net.sakuragame.eternal.kirraminer

import net.sakuragame.eternal.kirraminer.ore.DigMetadata
import net.sakuragame.eternal.kirraminer.ore.DigResult
import net.sakuragame.eternal.kirraminer.ore.DigState
import net.sakuragame.eternal.kirraminer.ore.Ore
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import org.bukkit.Location
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.adaptLocation
import taboolib.module.configuration.util.getLocation
import taboolib.module.configuration.util.setLocation
import taboolib.platform.util.toBukkitLocation

object Loader {

    @Awake(LifeCycle.ENABLE)
    fun i() {
        printToConsole("-- 正在加载矿物信息.")
        KirraMinerAPI.recycleAllMineEntities()
        KirraMinerAPI.ores.clear()
        KirraMinerAPI.oreMetadata.clear()
        KirraMiner.oresFile.getKeys(false).forEach {
            val digMetadataList = mutableListOf<DigMetadata>()
            val loc = KirraMiner.oresFile.getLocation("$it.loc")?.toBukkitLocation() ?: return@forEach
            val refreshTime = IntInterval.fromString(KirraMiner.oresFile.getString("$it.refresh-time") ?: return@forEach) ?: return@forEach
            val metadataSection = KirraMiner.oresFile.getConfigurationSection("$it.metadata-list") ?: return@forEach
            metadataSection.getKeys(false).forEach metaForeach@{ section ->
                val weight = KirraMiner.oresFile.getInt("$it.metadata-list.$section.weight")
                val name = KirraMiner.oresFile.getString("$it.metadata-list.$section.name") ?: return@metaForeach
                val digLevel = KirraMiner.oresFile.getInt("$it.metadata-list.$section.dig-level")
                val digTime = KirraMiner.oresFile.getInt("$it.metadata-list.$section.dig-time")
                val digResult = DigResult.fromString(KirraMiner.oresFile.getString("$it.metadata-list.$section.dig-result") ?: return@metaForeach) ?: return@metaForeach
                digMetadataList += DigMetadata(weight, name, digLevel, digTime, digResult)
            }
            KirraMinerAPI.oreMetadata[it] = digMetadataList
            KirraMinerAPI.ores[it] = Ore(it, loc, refreshTime, KirraMinerAPI.getWeightRandomMetadataByString(it) ?: return@forEach, DigState(entity = null, isDigging = false, isRefreshing = false, futureRefreshMillis = System.currentTimeMillis()))
        }
        printToConsole("-- 加载完毕, 一共加载了 ${KirraMinerAPI.ores.size} 个矿物.")
    }

    fun addOre(id: String, loc: Location, refreshTime: IntInterval) {
        KirraMiner.oresFile.setLocation("$id.loc", adaptLocation(loc))
        KirraMiner.oresFile["$id.refresh-time"] = refreshTime.toString()
        // 填充默认元数据模板.
        KirraMiner.oresFile["$id.metadata-list.example-item.weight"] = 10
        KirraMiner.oresFile["$id.metadata-list-example-item.name"] = "测试矿物."
        KirraMiner.oresFile["$id.metadata-list.example-item.dig-level"] = 1
        KirraMiner.oresFile["$id.metadata-list.example-item.dig-time"] = 10
        KirraMiner.oresFile["$id.metadata-list.example-item.dig-result"] = "ExampleItem, 4-8"
        KirraMiner.oresFile.saveToFile(KirraMiner.oresFile.file)
        i()
    }
}