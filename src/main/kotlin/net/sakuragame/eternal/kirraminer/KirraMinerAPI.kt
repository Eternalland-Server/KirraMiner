package net.sakuragame.eternal.kirraminer

import net.sakuragame.eternal.kirraminer.ore.DigMetadata
import net.sakuragame.eternal.kirraminer.ore.Ore
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.metadata.FixedMetadataValue
import taboolib.common5.RandomList
import java.util.*

@Suppress("SpellCheckingInspection")
object KirraMinerAPI {

    private const val MINE_ENTITY_IDENTIFIER = "KIRRAMINER_ENTITY"

    val ores = mutableMapOf<String, Ore>()

    val oreMetadataMap = mutableMapOf<String, List<DigMetadata>>()

    /**
     * 根据生物 UUID 获取相应的矿物实例.
     *
     * @param uuid 生物 UUID
     * @return 矿物实例.
     */
    fun getOreByEntityUUID(uuid: UUID) = KirraMinerAPI.ores.values.firstOrNull { uuid == it.digState.entity?.uniqueId }

    /**
     * 回收所有的矿物实体.
     */
    fun recycleAllMineEntities() {
        Bukkit.getWorlds().map { it.entities }.forEach {
            it.forEach { entity ->
                if (entity is ArmorStand && entity.hasMetadata(MINE_ENTITY_IDENTIFIER)) {
                    entity.remove()
                }
            }
        }
    }

    /**
     * 根据字符串 (ID) 来获取权重挖掘元数据.
     *
     * @param str 字符串
     * @return 挖掘元数据.
     */
    fun getWeightRandomMetadataByString(str: String): DigMetadata? {
        val metadataList = oreMetadataMap[str] ?: return null
        val weightList = RandomList<DigMetadata>()
        metadataList.forEach {
            weightList.add(it, it.weight)
        }
        return weightList.random()
    }

    /**
     * 生成一个矿物实体.
     *
     * @param name 实体名.
     * @param loc 实体坐标.
     * @return 实体实例.
     */
    fun generateOreEntity(name: String, loc: Location): ArmorStand {
        val armorStand = (loc.world.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand).also {
            it.setGravity(false)
            it.isMarker = true
            it.customName = name
            it.isCustomNameVisible = false
            it.setMetadata(MINE_ENTITY_IDENTIFIER, FixedMetadataValue(KirraMiner.plugin, ""))
        }
        return armorStand
    }
}