package net.sakuragame.eternal.kirraminer.ore

import org.bukkit.block.Block

data class DigState(var block: Block? = null, var isRefreshing: Boolean, var futureRefreshMillis: Long)