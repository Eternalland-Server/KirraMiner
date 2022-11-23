package net.sakuragame.eternal.kirraminer

import net.sakuragame.eternal.kirraminer.ore.DigMetadata
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
                val name = KirraMiner.conf.getString("settings.ores.$section.name") ?: return@metaForeach
                val oreIndex = KirraMiner.conf.getString("settings.ores.$section.index") ?: return@metaForeach
                val dropItem = KirraMiner.conf.getString("settings.ores.$section.drop") ?: return@metaForeach
                val costDurability = KirraMiner.conf.getIntRange("settings.ores.$section.cost-durability") ?: 1..1
                val providedExp = KirraMiner.conf.getIntRange("settings.ores.$section.provided-exp") ?: 0..0
                digMetadataList += DigMetadata(section, weight, name, oreIndex, dropItem, costDurability, providedExp)
            }

            KirraMinerAPI.oreMetadataMap[it] = digMetadataList
            val meta = KirraMinerAPI.getWeightRandomMetadataByID(it) ?: return@forEach
            val ore = Ore(
                id = it,
                isTemp = false,
                loc = loc,
                refreshTime = refreshTime,
                digState = DigState(block = null, isRefreshing = false, futureRefreshMillis = System.currentTimeMillis()),
                digMetadata = meta
            )
            KirraMinerAPI.addOre(ore.id, ore)
        }
    }
}