package net.sakuragame.eternal.kirraminer

import net.sakuragame.eternal.kirraminer.ore.DigMetadata
import net.sakuragame.eternal.kirraminer.ore.DigResult
import net.sakuragame.eternal.kirraminer.ore.DigState
import net.sakuragame.eternal.kirraminer.ore.Ore
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
            val loc = conf.getString("$it.loc")?.parseToLoc()
            val refreshTime = IntInterval.fromString(conf.getString("$it.refresh-time") ?: return@forEach) ?: return@forEach
            val metadataSection = conf.getConfigurationSection("$it.metadata-list") ?: return@forEach
            metadataSection.getKeys(false).forEach metaForeach@{ section ->
                val weight = conf.getInt("$it.metadata-list.$section.weight")
                val oreIndex = conf.getString("$it.metadata-list.$section.ore-index") ?: return@metaForeach
                val name = conf.getString("$it.metadata-list.$section.name") ?: return@metaForeach
                val digLevel = conf.getInt("$it.metadata-list.$section.dig-level")
                val digTime = conf.getInt("$it.metadata-list.$section.dig-time")
                val digResult = DigResult.fromString(conf.getString("$it.metadata-list.$section.dig-result") ?: return@metaForeach) ?: return@metaForeach
                digMetadataList += DigMetadata(weight, oreIndex, name, digLevel, digTime, digResult)
            }
            KirraMinerAPI.oreMetadataMap[it] = digMetadataList
            val meta = KirraMinerAPI.getWeightRandomMetadataByID(it) ?: return@forEach
            val ore = Ore(id = it,
                isTemp = false,
                loc = loc,
                refreshTime = refreshTime,
                digState = DigState(block = null, isRefreshing = false, futureRefreshMillis = System.currentTimeMillis()),
                digMetadata = meta,
                hologram = null
            )
            KirraMinerAPI.addOre(ore.id, ore)
        }
    }
}