package net.sakuragame.eternal.kirraminer.ore

import net.sakuragame.eternal.justlevel.api.JustLevelAPI
import net.sakuragame.eternal.kirraminer.*
import net.sakuragame.eternal.kirraminer.event.MineEndEvent
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.submit

data class Ore(
    val id: String,
    val isTemp: Boolean = false,
    val loc: Location?,
    val refreshTime: IntInterval,
    val digState: DigState,
    var digMetadata: DigMetadata,
) {

    fun afterDig(player: Player, item: ItemStack) {
        val costResult = costDurability(player, item)
        if (!costResult) {
            return
        }
        submit(delay = 2L) {
            player.playSound(player.location, Sound.BLOCK_ANVIL_BREAK, 1f, 1.5f)
        }
        submit(delay = 5L) {
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f)
        }
        giveResult(player)
        digState.futureRefreshMillis = System.currentTimeMillis() + (refreshTime.random * 1000)
        digState.isRefreshing = true
        player.playSound(player.location, Sound.BLOCK_STONE_STEP, 1f, 1.5f)
    }

    private fun costDurability(player: Player, item: ItemStack): Boolean {
        val costDurability = digMetadata.costDurability
            .random()
            .coerceAtLeast(1)
        val durability = getPickaxeDurability(item)?.minus(costDurability) ?: return false
        if (durability < 1) {
            player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1f, 1.5f)
            player.inventory.itemInMainHand = ItemStack(Material.AIR)
            return true
        }
        val newItem = setPickaxeDurability(player, item, durability) ?: return false
        val durabilityOnItem = getPickaxeDurabilityOnItem(durability, newItem)?.toShort() ?: return false
        player.inventory.itemInMainHand = newItem
        submit(async = false, delay = 3L) {
            player.inventory.itemInMainHand.durability = durabilityOnItem
            player.updateInventory()
        }
        return true
    }

    fun refresh() {
        digState.isRefreshing = false
        digState.futureRefreshMillis = System.currentTimeMillis()
        init()
    }

    private fun giveResult(player: Player) {
        // should not happen.
        if (loc == null) {
            return
        }
        submit(async = false) {
            val itemStack = digMetadata.digResult.getResultItem(player) ?: return@submit
            MineEndEvent(player, this@Ore, itemStack).call()
            val droppedItem = loc.world.dropItem(loc.clone().add(0.0, 0.6, 0.0), itemStack).apply {
                pickupDelay = 999999
                isGlowing = true
            }
            submit(delay = 7L) {
                player.collectItem(droppedItem)
                droppedItem.remove()
            }
            val providedExp = digMetadata.providedExp
            if (providedExp <= 0) {
                return@submit
            }
            JustLevelAPI.addExp(player.uniqueId, providedExp)
        }
    }

    private fun init() {
        digMetadata = KirraMinerAPI.getWeightRandomMetadataByID(id)!!
        if (digState.block != null) {
            digState.block?.remove()
            digState.block = null
        }
        submit(async = false) {
            digState.block = KirraMinerAPI.generateOreBlock(this@Ore)
        }
    }

    companion object {

        @Awake(LifeCycle.ACTIVE)
        fun i() {
            submit(async = true, period = 20L) {
                KirraMinerAPI.ores.values
                    .filter { it.digState.isRefreshing }
                    .filter { System.currentTimeMillis() >= it.digState.futureRefreshMillis }
                    .forEach {
                        submit(async = true) {
                            it.refresh()
                        }
                    }
            }
        }
    }
}