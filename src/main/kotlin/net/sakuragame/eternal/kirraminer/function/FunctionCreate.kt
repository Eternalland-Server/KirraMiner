package net.sakuragame.eternal.kirraminer.function

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.server.v1_12_R1.TileEntitySkull
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.chat.colored
import java.util.*

object FunctionCreate {

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        val item = e.item ?: return
        val block = e.clickedBlock ?: return
        val player = e.player
        if (!item.hasItemMeta() || !item.itemMeta.hasDisplayName() || !item.itemMeta.displayName.contains("矿物召唤魔杖")) {
            return
        }
        e.isCancelled = true
        when (e.action) {
            Action.LEFT_CLICK_BLOCK -> create(block, player, item)
            else -> return
        }
    }

    private fun create(block: Block, player: Player, item: ItemStack) {
        val num = item.itemMeta.lore[0].split(" - ")[0]
        val skullBlock = block.location.clone().add(0.0, 1.0, 0.0).block.apply {
            setType(Material.SKULL, true)
        }
        val entity = (skullBlock.world as CraftWorld).getTileEntityAt(skullBlock.x, skullBlock.y, skullBlock.z) as? TileEntitySkull ?: run {
            player.sendMessage("&c[System] &7无法转换成 tileEntity 数据.".colored())
            return
        }
        entity.gameProfile = GameProfile(UUID.randomUUID(), num).apply {
            properties.put("textures", Property("", ""))
            properties.put("model", Property("model", "model: $num"))
        }
        entity.update()
        skullBlock.state.update()
        player.sendMessage("&c[System] &7成功设置.".colored())
    }
}