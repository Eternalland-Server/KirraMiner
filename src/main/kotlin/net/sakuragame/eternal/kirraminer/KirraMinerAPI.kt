package net.sakuragame.eternal.kirraminer

import me.arasple.mc.trhologram.api.TrHologramAPI
import me.arasple.mc.trhologram.module.display.Hologram
import net.sakuragame.eternal.kirraminer.ore.DigMetadata
import net.sakuragame.eternal.kirraminer.ore.Ore
import net.sakuragame.eternal.kirraminer.ore.OreState
import net.sakuragame.eternal.kirraminer.ore.OreState.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.metadata.FixedMetadataValue
import taboolib.common5.RandomList
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs

@Suppress("SpellCheckingInspection")
object KirraMinerAPI {

    private const val MINE_ENTITY_IDENTIFIER = "KIRRAMINER_ENTITY"

    val ores = mutableMapOf<String, Ore>()

    val hologramMap = mutableMapOf<String, Hologram>()

    val oreMetadataMap = mutableMapOf<String, List<DigMetadata>>()

    /**
     * 获取两个坐标之间的随机坐标.
     * @param locA 坐标 A.
     * @param locB 坐标 B.
     * @param yLimit 坐标 Y 轴限制.
     * @return 随机坐标.
     */
    fun getRandomLocBetween2Loc(locA: Location, locB: Location, yLimit: Int): Location {
        return getRandomLocBetween2Loc(locA, locB, yLimit, 0)
    }

    private fun getRandomLocBetween2Loc(locA: Location, locB: Location, yLimit: Int, counts: Int): Location {
        val world = locA.world

        if (counts > 5) {
            error("获取随机坐标失败.")
        }

        val minX = locB.x.coerceAtMost(locA.x)
        val minZ = locB.z.coerceAtMost(locA.z)

        val maxX = locB.x.coerceAtLeast(locA.x)
        val maxZ = locB.z.coerceAtLeast(locA.z)

        val loc = Location(world, getRandomDouble(minX, maxX), getRandomDouble(yLimit.toDouble(), yLimit.toDouble()), getRandomDouble(minZ, maxZ))

        if (loc.block.type != Material.AIR) {
            return getRandomLocBetween2Loc(loc, locB, yLimit, counts + 1)
        }

        return loc
    }

    private fun getRandomDouble(min: Double, max: Double): Double {
        return min + ThreadLocalRandom.current().nextDouble(abs(max - min + 1))
    }

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
            it.forEach { entity ->
                if (entity is ArmorStand && entity.hasMetadata(MINE_ENTITY_IDENTIFIER)) {
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
        hologramMap[ore.id] = when (state) {
            IDLE -> TrHologramAPI.builder(ore.loc.clone().add(0.0, 3.4, 0.0))
                .append({
                    resultItem
                })
                .append("&f&l${ore.digMetadata.digEntityName.idle}".colored())
                .append("&7矿镐要求等级: &f${ore.digMetadata.digLevel}".colored())
                .append(" ")
                .append("&7掉落物品: ".colored())
                .append("&f- &e${resultItem.itemMeta.displayName.uncolored()} &7(${ore.digMetadata.digResult.amount})".colored())
                .build()
            DIGGING -> TrHologramAPI.builder(ore.loc.clone().add(0.0, 2.5, 0.0))
                .append({
                    resultItem
                })
                .append("&7正在收集... &f[${profile!!.player.name}]".colored())
                .append("&8( ${profile.getDiggingProgressBar() ?: ""} &8)".colored())
                .build()
            COOLDOWN -> TrHologramAPI.builder(ore.loc.clone().add(0.0, 2.0, 0.0))
                .append({
                    resultItem
                })
                .append("&7正在冷却.".colored())
                .append("&7将在 &f${getTextureDate(ore.digState.futureRefreshMillis)} &7刷新.".colored())
                .append(" ")
                .build()
            FINAL -> TrHologramAPI.builder(ore.loc.clone().add(0.0, 2.4, 0.0))
                .append({
                    resultItem
                })
                .append("&7挖掘完成! &a&l✓".colored())
                .append(" ")
                .build()
        }.apply {
            Bukkit.getOnlinePlayers().forEach {
                refreshVisibility(it)
            }
        }
    }

    /**
     * 生成一个矿物实体.
     *
     * @param ore 矿物实例.
     * @return 实体实例.
     */
    fun generateOreEntity(ore: Ore, state: OreState): ArmorStand {
        val loc = ore.loc
        val armorStand = (loc.world.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand).also {
            it.isGlowing = true
            it.setGravity(false)
            it.customName = "&r${state.getString(ore)}".colored()
            it.setMetadata(MINE_ENTITY_IDENTIFIER, FixedMetadataValue(KirraMiner.plugin, ""))
        }
        generateHologram(ore, state, null)
        return armorStand
    }
}