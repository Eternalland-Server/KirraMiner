package net.sakuragame.eternal.kirraminer

import net.sakuragame.eternal.kirraminer.function.FunctionOreCreate
import net.sakuragame.eternal.kirraminer.ore.sub.IntInterval
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.expansion.createHelper
import taboolib.module.chat.colored

@Suppress("SpellCheckingInspection")
@CommandHeader(name = "KirraMiner", aliases = ["km", "kminer"])
object Commands {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&c[System] &7正在重载...".colored())
            Loader.i()
            sender.sendMessage("&c[System] &7重载完毕.".colored())
        }
    }

    @CommandBody
    val add = subCommand {
        dynamic(commit = "oreID") {
            dynamic(commit = "refreshTime") {
                execute<Player> { player, context, _ ->
                    val oreId = context.get(1)
                    val refreshTime = IntInterval.fromString(context.get(2))
                    if (refreshTime == null) {
                        player.sendMessage("&c[System] &7刷新时间格式不正确.".colored())
                        return@execute
                    }
                    Loader.addOre(oreId, player.location, refreshTime)
                }
            }
        }
    }

    @CommandBody
    val setLoc = subCommand {
        dynamic(commit = "type") {
            execute<Player> { player, context, _ ->
                when (context.get(1).lowercase()) {
                    "a" -> {
                        FunctionOreCreate.setLocA(player.location)
                        player.sendMessage("&c[System] &7设置 A 点成功.".colored())
                        return@execute
                    }
                    "b" -> {
                        FunctionOreCreate.setLocB(player.location)
                        player.sendMessage("&c[System] &7设置 B 点成功.".colored())
                        return@execute
                    }
                    "ylimit" -> {
                        val yLimit = context.getOrNull(2)?.toIntOrNull()
                        if (yLimit == null) {
                            player.sendMessage("&c[System] &7错误格式.".colored())
                            return@execute
                        }
                        FunctionOreCreate.setYLimit(yLimit)
                        player.sendMessage("&c[System] &7设置 Y 轴限制成功.".colored())
                    }
                    else -> player.sendMessage("&c[System] &7错误格式.".colored())
                }
            }
        }
    }

    @CommandBody
    val setOreLoc = subCommand {
        dynamic(commit = "oreId") {
            execute<Player> { player, context, _ ->
                val ore = KirraMinerAPI.ores[context.get(1)]
                if (ore == null) {
                    player.sendMessage("&c[System] &7矿物不存在.".colored())
                    return@execute
                }
                val oreLoc = FunctionOreCreate.getLoc()
                if (oreLoc == null) {
                    player.sendMessage("&c[System] &7坐标不存在.".colored())
                    return@execute
                }
                Loader.setLoc(ore.id, oreLoc)
            }
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("&c[System] &7当前矿物列表.".colored())
            sender.sendMessage("&c[System] &7${KirraMinerAPI.ores.keys}".colored())
        }
    }
}