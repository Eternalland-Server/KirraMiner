package net.sakuragame.eternal.kirraminer

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import eu.decentsoftware.holograms.api.DHAPI
import net.minecraft.server.v1_12_R1.BlockPosition
import net.minecraft.server.v1_12_R1.TileEntitySkull
import net.sakuragame.eternal.kirraminer.ore.DigMetadata
import net.sakuragame.eternal.kirraminer.ore.DigState
import net.sakuragame.eternal.kirraminer.ore.Ore
import net.sakuragame.eternal.kirraminer.ore.OreState
import net.sakuragame.eternal.kirraminer.ore.OreState.*
import net.sakuragame.eternal.waypoints.api.WaypointsAPI
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Skull
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import taboolib.common5.RandomList
import taboolib.module.chat.colored
import taboolib.module.chat.uncolored
import taboolib.platform.util.title
import java.util.*
import java.util.concurrent.ConcurrentHashMap


@Suppress("SpellCheckingInspection")
object KirraMinerAPI {

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
     * 获取离玩家最近的矿石
     *
     * @param player 玩家
     * @param id 矿物 id
     */
    fun getNearestOreOfPlayer(player: Player, id: String): Ore? {
        val ore = ores.values
            .filter { it.loc != null }
            .filter { !it.digState.isRefreshing }
            .minByOrNull { it.loc!!.distanceSquared(player.location) } ?: return null
        val loc = ore.loc!!.clone().add(0.0, 2.0, 1.0)
        WaypointsAPI.navPointer(player, "ore", loc, 5.0, listOf(ore.digMetadata.entityName))
        player.title("", "&e&l已为您展示距离您最近的${ore.digMetadata.entityName}坐标".colored(), 10, 20, 5)
        return ore
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
    @Suppress("MemberVisibilityCanBePrivate")
    fun removeOre(id: String) {
        val ore = ores[id] ?: return
        ore.hologram?.destroy()
        ore.digState.block?.remove()
        ores.remove(id)
    }

    /**
     * 移除该世界的所有矿物
     *
     * @param world 世界
     */
    fun removeAllOresInWorld(world: World) {
        val armorStands = world.entities.filterIsInstance<ArmorStand>()
        val worldUid = armorStands.getOrNull(0)?.world?.uid ?: return
        ores.forEach { (id, ore) ->
            if (ore.loc?.world?.uid == worldUid) {
                removeOre(id)
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
            digState = DigState(block = null, isRefreshing = false, futureRefreshMillis = System.currentTimeMillis())
        )
        addOre(id, ore)
        return true
    }

    /**
     * 根据坐标获取相应的矿物实例
     *
     * @param loc 坐标
     * @return 矿物实例
     */
    fun getOreByLocation(loc: Location): Ore? {
        return ores.values.find { it.loc?.equals(loc) ?: false }
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
     * 获取权重挖掘元数据
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
     * 生成全息实体
     */
    fun generateHologram(ore: Ore, state: OreState) {
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
            IDLE -> DHAPI.createHologram(
                uuid, ore.loc.clone().add(0.0, 3.4, 0.0), listOf(
                    iconStr,
                    "&f&l${ore.digMetadata.entityName}",
                    "&7矿镐要求等级: &f${ore.digMetadata.digLevel}",
                    "",
                    "&7掉落物品: ",
                    "&f- &e${resultItem.itemMeta.displayName.uncolored()} &7(${ore.digMetadata.digResult.amount})",
                    ""
                )
            )

            FINAL -> DHAPI.createHologram(
                uuid, ore.loc.clone().add(0.0, 2.5, 0.0),
                listOf(
                    iconStr,
                    "&7挖掘完成! &a&l✓",
                    " "
                )
            )

            COOLDOWN -> DHAPI.createHologram(
                uuid, ore.loc.clone().add(0.0, 2.5, 0.0),
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
     * 生成矿物实体
     */
    fun generateOreBlock(ore: Ore, state: OreState): Block? {
        if (ore.loc == null) {
            return null
        }
        val loc = ore.loc.clone().apply {
            block.type = Material.SKULL
        }
        val skull = createSkullBlock(ore.digMetadata.oreIndex, loc) ?: return null
        generateHologram(ore, state)
        return skull.block
    }


    private fun createSkullBlock(id: String, loc: Location): Skull? {
        val skull = loc.block.state as Skull
//        skull.skullType = SkullType.PLAYER
        val skullTile = (skull.world as CraftWorld).handle
            .getTileEntity(BlockPosition(skull.x, skull.y, skull.z)) as? TileEntitySkull ?: return null
        skullTile.gameProfile = GameProfile(UUID.randomUUID(), null).apply {
            properties.put("model", Property("model", "model: $id"))
        }
        skullTile.update()
        skull.update(true)
        return skull
    }
}