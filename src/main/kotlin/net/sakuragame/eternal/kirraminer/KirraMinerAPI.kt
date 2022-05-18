package net.sakuragame.eternal.kirraminer

import eu.decentsoftware.holograms.api.DHAPI
import net.sakuragame.eternal.kirraminer.ore.DigMetadata
import net.sakuragame.eternal.kirraminer.ore.DigState
import net.sakuragame.eternal.kirraminer.ore.Ore
import net.sakuragame.eternal.kirraminer.ore.OreState
import net.sakuragame.eternal.kirraminer.ore.OreState.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.metadata.FixedMetadataValue
import taboolib.common5.RandomList
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("SpellCheckingInspection")
object KirraMinerAPI {

    const val MINE_ENTITY_IDENTIFIER = "KIRRAMINER_ENTITY"

    val ores = ConcurrentHashMap<String, Ore>()

    val oreMetadataMap = mutableMapOf<String, List<DigMetadata>>()

    /**
     * 获取矿物的 mapId
     *
     * @param ore 矿物
     */
    fun getOreMapId(ore: Ore): String? {
        ores.forEach { (id, value) ->
            val loc = value.loc ?: return@forEach
            if (loc == ore.loc!!) {
                return id
            }
        }
        return null
    }

    /**
     * 增加一个实例矿物
     *
     * @param id id
     * @param ore 矿物
     */
    fun addOre(id: String, ore: Ore) {
        ores[id] = ore.apply {
            if (loc == null) {
                return@apply
            }
            refresh()
        }
    }

    /**
     * 移除一个实例矿物
     *
     * @param id
     */
    fun removeOre(id: String) {
        val ore = ores[id] ?: return
        ore.hologram?.destroy()
        ore.digState.entity?.remove()
        ores.remove(id)
    }

    /**
     * 移除该世界的所有矿物
     *
     * @param world 世界
     */
    fun removeOresOfWorld(world: World) {
        val armorStands = world.entities.filterIsInstance<ArmorStand>()
        val worldUid = armorStands.getOrNull(0)?.world?.uid ?: return
        KirraMinerAPI.ores.forEach { (id, ore) ->
            if (ore.loc?.world?.uid == worldUid) {
                KirraMinerAPI.removeOre(id)
            }
        }
    }

    /**
     * 根据模板创建一个新的临时矿物
     *
     * @param id 矿物 id
     * @param templateOre 模板矿物
     * @param loc 坐标
     * @return 是否创建成功
     */
    fun createTempOre(id: String, templateOre: Ore, loc: Location): Boolean {
        if (templateOre.isTemp || ores.containsKey(id)) {
            return false
        }
        val templateId = templateOre.id
        val weightedMeta = getWeightRandomMetadataByID(templateId) ?: return false
        val ore = templateOre.copy(
            id = templateOre.id,
            isTemp = true,
            loc = loc,
            digMetadata = weightedMeta,
            digState = DigState(entity = null, isDigging = false, isRefreshing = false, futureRefreshMillis = System.currentTimeMillis())
        )
        addOre(id, ore)
        return true
    }

    /**
     * 根据生物 UUID 获取相应的矿物实例
     *
     * @param uuid 生物 UUID
     * @return 矿物实例
     */
    fun getOreByEntityUUID(uuid: UUID): Ore? {
        return ores.values.find { it.digState.entity?.uniqueId == uuid }
    }

    /**
     * 回收所有的矿物实体, 包括全息字
     */
    fun recycleAllOres() {
        Bukkit.getWorlds().map { it.entities }.forEach {
            it.filterIsInstance<ArmorStand>().forEach { entity ->
                if (entity.customName.contains("@*")) {
                    entity.remove()
                }
            }
        }
        ores.values.forEach {
            it.hologram?.destroy()
        }
    }

    /**
     * 根据 ID 来获取权重挖掘元数据
     *
     * @param id 字符串
     * @return 挖掘元数据
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
     * @param ore 矿物实例
     * @param state 矿物状态
     * @param profile 玩家档案
     */
    fun generateHologram(ore: Ore, state: OreState, profile: Profile?) {
        if (ore.loc == null) {
            return
        }
        ore.hologram?.destroy()
        ore.hologram = null
        val resultItem = ore.digMetadata.digResult.getResultItem(null)!!.apply {
            amount = 1
        }
        val iconStr = "#ICON: PAPER {zaphkiel:{a:${ore.digMetadata.digResult.itemId}}}"
        val uuid = UUID.randomUUID().toString()
        ore.hologram = when (state) {
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
     * 生成一个矿物实体
     *
     * @param ore 矿物实例
     * @return 实体实例
     */
    fun generateOreEntity(ore: Ore, state: OreState): ArmorStand? {
        if (ore.loc == null) {
            return null
        }
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