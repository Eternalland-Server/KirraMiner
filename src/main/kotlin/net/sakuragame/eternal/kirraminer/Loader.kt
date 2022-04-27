package net.sakuragame.eternal.kirraminer

import net.sakuragame.eternal.kirraminer.function.FunctionDigListener
import net.sakuragame.eternal.kirraminer.ore.*
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

object Loader {

    private val folder by lazy {
        File(KirraMiner.plugin.dataFolder, "ores")
    }

    @Awake(LifeCycle.ENABLE)
    fun i() {
        printToConsole("-- 正在加载矿物信息.")
        FunctionDigListener.baffle.reset()
        KirraMinerAPI.recycleAllOres()
        KirraMinerAPI.ores.clear()
        KirraMinerAPI.oreMetadataMap.clear()
        if (!folder.exists()) {
            folder.mkdirs()
            return
        }
        val yamlFiles = folder.listFiles()!!.map { Configuration.loadFromFile(it, Type.YAML) }
        yamlFiles.forEach {
            readConfig(it)
        }
        printToConsole("-- 加载完毕, 一共加载了 ${KirraMinerAPI.ores.size} 个矿物.")
    }

    private fun readConfig(conf: ConfigFile) {
        conf.getKeys(false).forEach {
            val digMetadataList = mutableListOf<DigMetadata>()
            val oreLoc = conf.getOreLoc("$it.ore-loc") ?: return@forEach
            val loc = KirraMinerAPI.getRandomLocBetween2Loc(oreLoc.a, oreLoc.b, oreLoc.yLimit)
            val refreshTime = IntInterval.fromString(conf.getString("$it.refresh-time") ?: return@forEach) ?: return@forEach
            val metadataSection = conf.getConfigurationSection("$it.metadata-list") ?: return@forEach
            metadataSection.getKeys(false).forEach metaForeach@{ section ->
                val weight = conf.getInt("$it.metadata-list.$section.weight")
                val idleName = conf.getString("$it.metadata-list.$section.name.idle") ?: return@metaForeach
                val afterName = conf.getString("$it.metadata-list.$section.name.after") ?: return@metaForeach
                val digLevel = conf.getInt("$it.metadata-list.$section.dig-level")
                val digTime = conf.getInt("$it.metadata-list.$section.dig-time")
                val digResult = DigResult.fromString(conf.getString("$it.metadata-list.$section.dig-result") ?: return@metaForeach) ?: return@metaForeach
                val digEntityName = DigEntityName(idleName, afterName)
                digMetadataList += DigMetadata(weight, digEntityName, digLevel, digTime, digResult)
            }
            KirraMinerAPI.oreMetadataMap[it] = digMetadataList
            val randomMeta = KirraMinerAPI.getWeightRandomMetadataByID(it) ?: return@forEach
            KirraMinerAPI.ores[it] = Ore(it, loc, refreshTime, randomMeta, DigState(entity = null, isDigging = false, isRefreshing = false, futureRefreshMillis = System.currentTimeMillis())).apply {
                refresh()
            }
        }
    }

    private fun Configuration.getOreLoc(path: String): OreLocation? {
        val locA = getString("$path.a")?.parseToLoc() ?: return null
        val locB = getString("$path.b")?.parseToLoc() ?: return null
        val yLimit = getInt("$path.y-limit")
        return OreLocation(locA, locB, yLimit)
    }
}