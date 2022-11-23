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

    fun afterDig(player: Player, item: ItemStack, loc: Location?, refresh: Boolean = true) {
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
        giveResult(item, loc, player)
        if (refresh && refreshTime.min != -1) {
            digState.futureRefreshMillis = System.currentTimeMillis() + (refreshTime.random * 1000)
            digState.isRefreshing = true
        }
        player.playSound(player.location, Sound.BLOCK_STONE_STEP, 1f, 1.5f)
    }

    private fun costDurability(player: Player, item: ItemStack): Boolean {
        val costDurability = digMetadata.costDurability
            .random()
            .coerceAtLeast(1)
        val durability = getPickaxeDurability(item)?.minus(costDurability) ?: return false
        val pickaxeName = player.inventory.itemInMainHand.getZaphkielName() ?: return false
        val digTypes = getDigTypes(pickaxeName)
        val isInvalidMovement = !digTypes.contains("ALL") && !digTypes.contains(digMetadata.id)
        when {
            isInvalidMovement && durability < 1 -> {
                submit(delay = 3L) {
                    player.playSound(player.location, Sound.ITEM_SHIELD_BREAK, 1f, 1.5f)
                }
                removeItem(player)
                return false
            }
            isInvalidMovement -> {
                submit(delay = 3L) {
                    player.playSound(player.location, Sound.ITEM_SHIELD_BREAK, 1f, 1.5f)
                }
                return false
            }
            durability < 1 -> {
                removeItem(player)
                return true
            }
        }
        val newItem = setPickaxeDurability(player, item, durability) ?: return false
        val durabilityOnItem = getPickaxeDurabilityOnItem(newItem, durability) ?: return false
        player.inventory.itemInMainHand = newItem
        submit(async = false, delay = 3L) {
            player.inventory.itemInMainHand.durability = durabilityOnItem
            player.updateInventory()
        }
        return true
    }

    private fun removeItem(player: Player) {
        player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 1f, 1.5f)
        player.inventory.itemInMainHand = ItemStack(Material.AIR)
    }

    fun refresh() {
        digState.isRefreshing = false
        digState.futureRefreshMillis = System.currentTimeMillis()
        init()
    }

    private fun giveResult(item: ItemStack, loc: Location?, player: Player) {
        val finalLocation = this.loc ?: (loc ?: return)
        submit(async = false) {
            val itemStack = digMetadata.getResultItem(item) ?: return@submit
            MineEndEvent(player, this@Ore, itemStack).call()
            val droppedItem = finalLocation.world.dropItem(finalLocation.clone().add(0.0, 0.6, 0.0), itemStack).apply {
                pickupDelay = 999999
                isGlowing = true
            }
            submit(delay = 7L) {
                player.collectItem(droppedItem)
                droppedItem.remove()
            }
            val providedExp = digMetadata.provideExp.random()
            if (providedExp <= 0) {
                return@submit
            }
            JustLevelAPI.addExp(player.uniqueId, providedExp.toDouble())
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

        private fun getDigTypes(pickaxeName: String): List<String> {
            return KirraMiner.conf.getStringList("settings.pickaxe.$pickaxeName.dig-type")
        }
    }
}