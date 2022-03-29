package net.sakuragame.eternal.kirraminer

import net.sakuragame.eternal.kirraminer.function.FunctionDigListener
import net.sakuragame.eternal.kirraminer.ore.*
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import org.bukkit.Location
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.adaptLocation
import taboolib.module.configuration.util.getLocation
import taboolib.module.configuration.util.setLocation
import taboolib.platform.util.toBukkitLocation

object Loader {

    @Awake(LifeCycle.ACTIVE)
    fun i() {
        printToConsole("-- 正在加载矿物信息.")
        FunctionDigListener.baffle.reset()
        KirraMinerAPI.recycleAllMineEntities()
        KirraMinerAPI.ores.clear()
        KirraMinerAPI.oreMetadataMap.clear()
        KirraMiner.oresFile.getKeys(false).forEach {
            val digMetadataList = mutableListOf<DigMetadata>()
            val loc = KirraMiner.oresFile.getLocation("$it.loc")?.toBukkitLocation() ?: return@forEach
            val refreshTime = IntInterval.fromString(KirraMiner.oresFile.getString("$it.refresh-time") ?: return@forEach) ?: return@forEach
            val metadataSection = KirraMiner.oresFile.getConfigurationSection("$it.metadata-list") ?: return@forEach
            metadataSection.getKeys(false).forEach metaForeach@{ section ->
                val weight = KirraMiner.oresFile.getInt("$it.metadata-list.$section.weight")
                val idleName = KirraMiner.oresFile.getString("$it.metadata-list.$section.name.idle") ?: return@metaForeach
                val afterName = KirraMiner.oresFile.getString("$it.metadata-list.$section.name.after") ?: return@metaForeach
                val digLevel = KirraMiner.oresFile.getInt("$it.metadata-list.$section.dig-level")
                val digTime = KirraMiner.oresFile.getInt("$it.metadata-list.$section.dig-time")
                val digResult = DigResult.fromString(KirraMiner.oresFile.getString("$it.metadata-list.$section.dig-result") ?: return@metaForeach) ?: return@metaForeach
                val digEntityName = DigEntityName(idleName, afterName)
                digMetadataList += DigMetadata(weight, digEntityName, digLevel, digTime, digResult)
            }
            KirraMinerAPI.oreMetadataMap[it] = digMetadataList
            val randomMeta = KirraMinerAPI.getWeightRandomMetadataByString(it) ?: return@forEach
            KirraMinerAPI.ores[it] = Ore(it, loc, refreshTime, randomMeta, DigState(entity = null, isDigging = false, isRefreshing = false, futureRefreshMillis = System.currentTimeMillis())).apply {
                refresh()
            }
        }
        printToConsole("-- 加载完毕, 一共加载了 ${KirraMinerAPI.ores.size} 个矿物.")
    }

    fun addOre(id: String, loc: Location, refreshTime: IntInterval) {
        setLoc(id, loc)
        KirraMiner.oresFile["$id.refresh-time"] = refreshTime.toString()
        // 填充默认元数据模板.
        KirraMiner.oresFile["$id.metadata-list.example-item.weight"] = 10
        KirraMiner.oresFile["$id.metadata-list.example-item.name.idle"] = "铁矿"
        KirraMiner.oresFile["$id.metadata-list.example-item.name.after"] = "铁矿"
        KirraMiner.oresFile["$id.metadata-list.example-item.dig-level"] = 1
        KirraMiner.oresFile["$id.metadata-list.example-item.dig-time"] = 10
        KirraMiner.oresFile["$id.metadata-list.example-item.dig-result"] = "ExampleItem, 4-8"
        KirraMiner.oresFile.saveToFile(KirraMiner.oresFile.file)
        i()
    }

    fun setLoc(id: String, loc: Location) {
        KirraMiner.oresFile.setLocation("$id.loc", adaptLocation(loc))
    }
}