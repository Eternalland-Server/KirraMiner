package net.sakuragame.eternal.kirraminer

import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.holograms.Hologram
import net.sakuragame.eternal.kirraminer.ore.DigMetadata
import net.sakuragame.eternal.kirraminer.ore.Ore
import net.sakuragame.eternal.kirraminer.ore.OreState
import net.sakuragame.eternal.kirraminer.ore.OreState.*
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.metadata.FixedMetadataValue
import taboolib.common5.RandomList
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored
import java.util.*

@Suppress("SpellCheckingInspection")
object KirraMinerAPI {

    const val MINE_ENTITY_IDENTIFIER = "KIRRAMINER_ENTITY"

    val ores = mutableMapOf<String, Ore>()

    val hologramMap = mutableMapOf<String, Hologram>()

    val oreMetadataMap = mutableMapOf<String, List<DigMetadata>>()

    /**
     * 根据生物 UUID 获取相应的矿物实例.
     *
     * @param uuid 生物 UUID
     * @return 矿物实例.
     */
    fun getOreByEntityUUID(uuid: UUID) = ores.values.firstOrNull { uuid == it.digState.entity?.uniqueId }

    /**
     * 回收所有的矿物实体, 包括全息字.
     */
    fun recycleAllOres() {
        Bukkit.getWorlds().map { it.entities }.forEach {
            it.filterIsInstance<ArmorStand>().forEach { entity ->
                if (entity.customName.contains("@*")) {
                    entity.remove()
                }
            }
        }
        hologramMap.values.forEach {
            it.destroy()
        }
        hologramMap.clear()
    }

    /**
     * 根据 ID 来获取权重挖掘元数据.
     *
     * @param id 字符串.
     * @return 挖掘元数据.
     */
    fun getWeightRandomMetadataByID(id: String): DigMetadata? {
        val metadataList = oreMetadataMap[id] ?: return null
        val weightList = RandomList<DigMetadata>()
        metadataList.forEach {
            weightList.add(it, it.weight)
        }
        return weightList.random()
    }

    /**
     * 生成全息实体.
     * @param ore 矿物实例.
     * @param state 矿物状态.
     * @param profile 玩家档案.
     */
    fun generateHologram(ore: Ore, state: OreState, profile: Profile?) {
        val resultItem = ore.digMetadata.digResult.getResultItem(null)!!.apply {
            amount = 1
        }
        if (hologramMap[ore.id] != null) {
            val hologram = hologramMap[ore.id]!!
            hologram.destroy()
            hologramMap -= ore.id
        }
        val iconStr = "#ICON: PAPER {zaphkiel:{a:${ore.digMetadata.digResult.itemId}}}"
        val uuid = UUID.randomUUID().toString()
        hologramMap[ore.id] = when (state) {
            IDLE -> DHAPI.createHologram(uuid, ore.loc.clone().add(0.0, 3.4, 0.0), listOf(
                iconStr,
                "&f&l${ore.digMetadata.digEntityName.idle}",
                "&7矿镐要求等级: &f${ore.digMetadata.digLevel}",
                "",
                "&7掉落物品: ",
                "&f- &e${resultItem.itemMeta.displayName.uncolored()} &7(${ore.digMetadata.digResult.amount})",
                ""))
            DIGGING -> DHAPI.createHologram(uuid, ore.loc.clone().add(0.0, 2.5, 0.0),
                listOf(
                    iconStr,
                    "&7正在收集... &f[${profile!!.player.name}]",
                    "&8( ${profile.getDiggingProgressBar() ?: ""} &8)"
                )
            )
            FINAL -> DHAPI.createHologram(uuid, ore.loc.clone().add(0.0, 2.5, 0.0),
                listOf(
                    iconStr,
                    "&7挖掘完成! &a&l✓",
                    " "
                ))
            COOLDOWN -> DHAPI.createHologram(uuid, ore.loc.clone().add(0.0, 2.5, 0.0),
                listOf(
                    iconStr,
                    "&7正在冷却.",
                    "&7将在 &f${getTextureDate(ore.digState.futureRefreshMillis)} &7刷新.",
                    " "
                )
            )
        }
    }

    /**
     * 生成一个矿物实体.
     *
     * @param ore 矿物实例.
     * @return 实体实例.
     */
    fun generateOreEntity(ore: Ore, state: OreState): ArmorStand {
        val loc = ore.loc.clone()
        val armorStand = (loc.world.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand).also {
            it.setGravity(false)
            it.customName = "&8&l&o${state.getString(ore)}@*".colored()
            it.setMetadata(MINE_ENTITY_IDENTIFIER, FixedMetadataValue(KirraMiner.plugin, ""))
        }
        generateHologram(ore, state, null)
        return armorStand
    }
}