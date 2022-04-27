package net.sakuragame.eternal.kirraminer.event

import net.sakuragame.eternal.kirraminer.ore.Ore
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.type.BukkitProxyEvent

class MineEndEvent(val player: Player, val ore: Ore, val item: ItemStack) : BukkitProxyEvent()