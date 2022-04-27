package net.sakuragame.eternal.kirraminer.event

import net.sakuragame.eternal.kirraminer.ore.Ore
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

class MineStartEvent(val player: Player, val ore: Ore) : BukkitProxyEvent()