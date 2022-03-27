package net.sakuragame.eternal.kirraminer.ore

import org.bukkit.entity.ArmorStand

data class DigState(var entity: ArmorStand? = null, var isDigging: Boolean, var isRefreshing: Boolean, var futureRefreshMillis: Long)